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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Payment Service
 *
 * Two modes of operation:
 *
 * 1. REAL MODE (production) — Stripe keys configured
 *    - Creates real Stripe PaymentIntents
 *    - Returns clientSecret to frontend
 *    - Receives webhooks from Stripe on payment confirmation
 *
 * 2. SIMULATION MODE (development) — no real Stripe keys needed
 *    - Creates payment record in PENDING status
 *    - Call POST /api/v1/payments/{orderId}/simulate-success
 *      to manually trigger payment confirmation
 *    - Perfect for testing the full flow without real cards
 *
 * The service auto-detects which mode based on whether
 * stripe.secret-key starts with "sk_test_REPLACE" (placeholder)
 * or a real key value.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${stripe.secret-key:not-configured}")
    private String stripeSecretKey;

    @Value("${stripe.platform-account-id:not-configured}")
    private String platformAccountId;

    /**
     * Called when "order.placed" Kafka event is received.
     * Creates a payment record and a Stripe PaymentIntent.
     */
    @Transactional
    public PaymentDto.PaymentResponse createPayment(PaymentDto.OrderPlacedEvent event) {
        log.info("Creating payment for order: {} amount: ${}",
            event.getOrderId(), event.getTotalAmount());

        // Check if payment already exists (idempotency — Kafka may redeliver)
        if (paymentRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.warn("Payment already exists for order: {}", event.getOrderId());
            Payment existing = paymentRepository.findByOrderId(event.getOrderId()).get();
            return toResponse(existing);
        }

        BigDecimal platformFee = event.getPlatformFeeAmount() != null
            ? event.getPlatformFeeAmount()
            : event.getTotalAmount().multiply(new BigDecimal("0.15"))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal restaurantAmount = event.getTotalAmount()
            .subtract(platformFee)
            .setScale(2, RoundingMode.HALF_UP);

        Payment payment = Payment.builder()
            .orderId(event.getOrderId())
            .customerId(event.getCustomerId())
            .restaurantId(event.getRestaurantId())
            .totalAmount(event.getTotalAmount())
            .platformFeeAmount(platformFee)
            .restaurantAmount(restaurantAmount)
            .stripeTransferGroup("order-" + event.getOrderId())
            .status(PaymentStatus.PENDING)
            .build();

        // Try real Stripe integration — fall back to simulation if not configured
        if (isStripeConfigured()) {
            createStripePaymentIntent(payment, event);
        } else {
            log.info("Stripe not configured — using simulation mode for order: {}",
                event.getOrderId());
            payment.setStripePaymentIntentId("sim_" + UUID.randomUUID());
            payment.setStripeClientSecret("sim_secret_" + UUID.randomUUID());
        }

        Payment saved = paymentRepository.save(payment);
        log.info("Payment record created: {} status: {}", saved.getId(), saved.getStatus());
        return toResponse(saved);
    }

    /**
     * Simulate payment success — for local development without real Stripe.
     * Call this from Postman after placing an order.
     *
     * This triggers the same Kafka event that a real Stripe webhook would,
     * so order-service updates the order status to CONFIRMED automatically.
     */
    @Transactional
    public PaymentDto.PaymentResponse simulatePaymentSuccess(UUID orderId) {
        log.info("Simulating payment success for order: {}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException(
                "Payment not found for order: " + orderId
            ));

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            log.warn("Payment already succeeded for order: {}", orderId);
            return toResponse(payment);
        }

        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setPaidAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        // Publish success event — order-service will update order to CONFIRMED
        publishPaymentSucceeded(saved);

        log.info("Payment simulated as successful for order: {}", orderId);
        return toResponse(saved);
    }

    /**
     * Simulate payment failure — for testing the failure path.
     */
    @Transactional
    public PaymentDto.PaymentResponse simulatePaymentFailure(UUID orderId, String reason) {
        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException(
                "Payment not found for order: " + orderId
            ));

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason != null ? reason : "Simulated payment failure");
        Payment saved = paymentRepository.save(payment);

        publishPaymentFailed(saved);
        log.info("Payment simulated as failed for order: {}", orderId);
        return toResponse(saved);
    }

    /**
     * Get payment details for an order.
     */
    @Transactional(readOnly = true)
    public PaymentDto.PaymentResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException(
                "Payment not found for order: " + orderId
            ));
        return toResponse(payment);
    }

    /**
     * Get all payments for a customer.
     */
    @Transactional(readOnly = true)
    public List<PaymentDto.PaymentSummaryResponse> getCustomerPayments(UUID customerId) {
        return paymentRepository
            .findByCustomerIdOrderByCreatedAtDesc(customerId)
            .stream()
            .map(this::toSummaryResponse)
            .collect(Collectors.toList());
    }

    /**
     * Handle real Stripe webhook — called by StripeWebhookController.
     * In production, Stripe calls this endpoint when payment is confirmed.
     */
    @Transactional
    public void handleStripeWebhook(String paymentIntentId, String eventType) {
        log.info("Stripe webhook received: {} for intent: {}", eventType, paymentIntentId);

        Payment payment = paymentRepository
            .findByStripePaymentIntentId(paymentIntentId)
            .orElseThrow(() -> new RuntimeException(
                "Payment not found for intent: " + paymentIntentId
            ));

        switch (eventType) {
            case "payment_intent.succeeded" -> {
                payment.setStatus(PaymentStatus.SUCCEEDED);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);
                publishPaymentSucceeded(payment);
            }
            case "payment_intent.payment_failed" -> {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Payment declined by card issuer");
                paymentRepository.save(payment);
                publishPaymentFailed(payment);
            }
            default -> log.debug("Unhandled Stripe event type: {}", eventType);
        }
    }

    // =====================================================
    // Private helpers
    // =====================================================

    private void createStripePaymentIntent(Payment payment, PaymentDto.OrderPlacedEvent event) {
        try {
            com.stripe.Stripe.apiKey = stripeSecretKey;

            // Amount in cents — Stripe uses smallest currency unit
            long amountInCents = payment.getTotalAmount()
                .multiply(new BigDecimal("100"))
                .longValue();

            long feeInCents = payment.getPlatformFeeAmount()
                .multiply(new BigDecimal("100"))
                .longValue();

            com.stripe.param.PaymentIntentCreateParams params =
                com.stripe.param.PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd")
                    .setApplicationFeeAmount(feeInCents)
                    .setTransferGroup(payment.getStripeTransferGroup())
                    .putMetadata("orderId", payment.getOrderId().toString())
                    .putMetadata("restaurantId", payment.getRestaurantId().toString())
                    .build();

            com.stripe.model.PaymentIntent intent =
                com.stripe.model.PaymentIntent.create(params);

            payment.setStripePaymentIntentId(intent.getId());
            payment.setStripeClientSecret(intent.getClientSecret());

            log.info("Stripe PaymentIntent created: {}", intent.getId());
        } catch (Exception e) {
            log.error("Stripe PaymentIntent creation failed", e);
            // Don't throw — save with PENDING status so it can be retried
            payment.setStripePaymentIntentId("stripe_error_" + UUID.randomUUID());
        }
    }

    private boolean isStripeConfigured() {
        return stripeSecretKey != null
            && !stripeSecretKey.equals("not-configured")
            && !stripeSecretKey.startsWith("sk_test_REPLACE");
    }

    private void publishPaymentSucceeded(Payment payment) {
        try {
            PaymentDto.PaymentSucceededEvent event = PaymentDto.PaymentSucceededEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .restaurantId(payment.getRestaurantId())
                .totalAmount(payment.getTotalAmount())
                .platformFeeAmount(payment.getPlatformFeeAmount())
                .restaurantAmount(payment.getRestaurantAmount())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .paidAt(payment.getPaidAt())
                .build();

            kafkaTemplate.send("payment.succeeded",
                payment.getOrderId().toString(), event);
            log.info("Published payment.succeeded for order: {}", payment.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish payment.succeeded event", e);
        }
    }

    private void publishPaymentFailed(Payment payment) {
        try {
            PaymentDto.PaymentFailedEvent event = PaymentDto.PaymentFailedEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .failureReason(payment.getFailureReason())
                .failedAt(LocalDateTime.now())
                .build();

            kafkaTemplate.send("payment.failed",
                payment.getOrderId().toString(), event);
            log.info("Published payment.failed for order: {}", payment.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish payment.failed event", e);
        }
    }

    private PaymentDto.PaymentResponse toResponse(Payment payment) {
        return PaymentDto.PaymentResponse.builder()
            .id(payment.getId())
            .orderId(payment.getOrderId())
            .customerId(payment.getCustomerId())
            .totalAmount(payment.getTotalAmount())
            .platformFeeAmount(payment.getPlatformFeeAmount())
            .restaurantAmount(payment.getRestaurantAmount())
            .status(payment.getStatus())
            .clientSecret(payment.getStripeClientSecret())
            .paidAt(payment.getPaidAt())
            .createdAt(payment.getCreatedAt())
            .build();
    }

    private PaymentDto.PaymentSummaryResponse toSummaryResponse(Payment payment) {
        return PaymentDto.PaymentSummaryResponse.builder()
            .id(payment.getId())
            .orderId(payment.getOrderId())
            .totalAmount(payment.getTotalAmount())
            .status(payment.getStatus())
            .createdAt(payment.getCreatedAt())
            .build();
    }
}
