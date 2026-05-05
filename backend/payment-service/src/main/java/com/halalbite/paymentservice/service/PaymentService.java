package com.halalbite.paymentservice.service;

import com.halalbite.paymentservice.dto.PaymentDto;
import com.halalbite.paymentservice.entity.Payment;
import com.halalbite.paymentservice.entity.PaymentStatus;
import com.halalbite.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PayPalService payPalService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.frontend-url:http://localhost:8088}")
    private String frontendUrl;

    // =====================================================
    // Initiate payment — called after order is placed
    // =====================================================

    @Transactional
    public PaymentDto.PaymentResponse initiatePayment(
            String orderId,
            BigDecimal amount,
            String customerId) {

        log.info("Initiating PayPal payment for order: {}", orderId);
        // Duplicate guard — Kafka can redeliver, frontend may also call this
        Optional<Payment> existing = paymentRepository
                .findByOrderId(UUID.fromString(orderId));
        if (existing.isPresent()) {
            log.info("Payment already exists for order: {} status: {} — returning existing",
                    orderId, existing.get().getStatus());
            return toResponse(existing.get());
        }
        // Try PayPal first, fall back to simulation if it fails
        try {
            String returnUrl = frontendUrl + "/payment/success?orderId=" + orderId;
            String cancelUrl = frontendUrl + "/payment/cancel?orderId=" + orderId;

            com.paypal.api.payments.Payment paypalPayment =
                    payPalService.createPayment(amount, "USD", orderId, returnUrl, cancelUrl);

            String approvalUrl = paypalPayment.getLinks().stream()
                    .filter(link -> "approval_url".equals(link.getRel()))
                    .findFirst()
                    .map(com.paypal.api.payments.Links::getHref)
                    .orElseThrow(() -> new RuntimeException("No approval URL in PayPal response"));

            Payment payment = Payment.builder()
                    .orderId(UUID.fromString(orderId))
                    .customerId(customerId != null ? UUID.fromString(customerId) : null)
                    .amount(amount)
                    .currency("USD")
                    .status(PaymentStatus.PENDING)
                    .approvalUrl("SIMULATION_MODE")
                    .failureReason(null)
                    .paypalPaymentId(paypalPayment.getId())
                    .approvalUrl(approvalUrl)
                    .build();

            Payment saved = paymentRepository.save(payment);
            log.info("Payment saved: {} approvalUrl: {}", saved.getId(), approvalUrl);

            // Auto-confirm in simulation mode after 3 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    simulatePaymentSuccess(saved.getOrderId());
                } catch (Exception ex) {
                    log.error("Auto-confirm failed", ex);
                }
            }).start();

            return toResponse(saved);

        } catch (Exception e) {
            log.error("PayPal payment creation failed for order {}: {}", orderId, e.getMessage());
            publishPaymentFailed(orderId, e.getMessage());
            throw new RuntimeException("Payment initiation failed: " + e.getMessage(), e);
        }
    }

    // ADD — getMyPayments endpoint needs this:
    @Transactional(readOnly = true)
    public List<PaymentDto.PaymentSummaryResponse> getCustomerPayments(UUID customerId) {
        return paymentRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    // ADD — summary mapper:
    private PaymentDto.PaymentSummaryResponse toSummaryResponse(Payment payment) {
        return PaymentDto.PaymentSummaryResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    // =====================================================
    // Execute payment — called when PayPal redirects back
    // =====================================================

    @Transactional
    public PaymentDto.PaymentResponse executePayment(
            String paypalPaymentId,
            String payerId,
            String orderId) {

        log.info("Executing PayPal payment: {} payerId: {} orderId: {}",
            paypalPaymentId, payerId, orderId);

        try {
            com.paypal.api.payments.Payment executed =
                payPalService.executePayment(paypalPaymentId, payerId);

            Payment payment = paymentRepository
                .findByOrderId(UUID.fromString(orderId))
                .orElseThrow(() -> new RuntimeException(
                    "Payment not found for order: " + orderId));

            if ("approved".equals(executed.getState())) {
                payment.setStatus(PaymentStatus.SUCCEEDED);
                payment.setPaypalPayerId(payerId);
                paymentRepository.save(payment);
                publishPaymentSucceeded(orderId, payment.getAmount());
                log.info("Payment SUCCEEDED for order: {}", orderId);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("PayPal state: " + executed.getState());
                paymentRepository.save(payment);
                publishPaymentFailed(orderId, "PayPal state: " + executed.getState());
                log.warn("Payment FAILED for order: {} state: {}", orderId, executed.getState());
            }

            return toResponse(payment);

        } catch (Exception e) {
            log.error("PayPal execute failed for order {}: {}", orderId, e.getMessage());
            publishPaymentFailed(orderId, e.getMessage());
            throw new RuntimeException("Payment execution failed: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // Get payment by order ID
    // =====================================================

    @Transactional(readOnly = true)
    public PaymentDto.PaymentResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository
                .findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException(
                        "Payment not found for order: " + orderId));
        return toResponse(payment);
    }

    // =====================================================
    // Kafka publishers
    // =====================================================

    private void publishPaymentSucceeded(String orderId, BigDecimal amount) {
        kafkaTemplate.send("payment.succeeded", Map.of(
            "orderId", orderId,
            "amount", amount.toString(),
            "timestamp", LocalDateTime.now().toString()
        ));
        log.info("Published payment.succeeded for order: {}", orderId);
    }

    private void publishPaymentFailed(String orderId, String reason) {
        kafkaTemplate.send("payment.failed", Map.of(
            "orderId", orderId,
            "reason", reason != null ? reason : "Unknown error",
            "timestamp", LocalDateTime.now().toString()
        ));
        log.info("Published payment.failed for order: {}", orderId);
    }

    // =====================================================
    // Private mapper — entity → DTO
    // =====================================================

    private PaymentDto.PaymentResponse toResponse(Payment payment) {
        return PaymentDto.PaymentResponse.builder()
            .id(payment.getId())
            .orderId(payment.getOrderId())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .status(payment.getStatus())
            .approvalUrl(payment.getApprovalUrl())
            .paypalPaymentId(payment.getPaypalPaymentId())
            .createdAt(payment.getCreatedAt())
            .build();
    }

    /**
     * DEV ONLY — simulate a successful payment without PayPal.
     * Useful for testing the Kafka chain locally.
     */
    @Transactional
    public PaymentDto.PaymentResponse simulatePaymentSuccess(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseGet(() -> {
                    // Create a dummy payment record if none exists
                    Payment p = Payment.builder()
                            .orderId(orderId)
                            .amount(new BigDecimal("10.00"))
                            .currency("USD")
                            .status(PaymentStatus.PENDING)
                            .build();
                    return paymentRepository.save(p);
                });

        payment.setStatus(PaymentStatus.SUCCEEDED);
        paymentRepository.save(payment);
        publishPaymentSucceeded(orderId.toString(), payment.getAmount());
        log.info("Simulated payment SUCCESS for order: {}", orderId);
        return toResponse(payment);
    }
// ========================================================
    /**
     * DEV ONLY — simulate a failed payment.
     */
 // ========================================================

    @Transactional
    public PaymentDto.PaymentResponse simulatePaymentFailure(UUID orderId, String reason) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseGet(() -> {
                    Payment p = Payment.builder()
                            .orderId(orderId)
                            .amount(new BigDecimal("10.00"))
                            .currency("USD")
                            .status(PaymentStatus.PENDING)
                            .build();
                    return paymentRepository.save(p);
                });

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        paymentRepository.save(payment);
        publishPaymentFailed(orderId.toString(), reason);
        log.info("Simulated payment FAILURE for order: {} reason: {}", orderId, reason);
        return toResponse(payment);
    }
}