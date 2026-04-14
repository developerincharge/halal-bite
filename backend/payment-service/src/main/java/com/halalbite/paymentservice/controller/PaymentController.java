package com.halalbite.paymentservice.controller;

import com.halalbite.paymentservice.dto.PaymentDto;
import com.halalbite.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Payment Controller
 *
 * Endpoints:
 *   GET    /api/v1/payments/order/{orderId}           → get payment for order
 *   GET    /api/v1/payments/my                        → customer payment history
 *   POST   /api/v1/payments/{orderId}/simulate-success → DEV ONLY — fake payment success
 *   POST   /api/v1/payments/{orderId}/simulate-failure → DEV ONLY — fake payment failure
 *   POST   /api/v1/payments/webhook                   → Stripe webhook (no auth)
 *
 * The simulate endpoints are your best friend during development.
 * Full payment flow without a real credit card:
 *   1. Place order → order.placed Kafka event fires
 *   2. Payment created automatically in PENDING status
 *   3. Call simulate-success → payment.succeeded Kafka event fires
 *   4. Order status changes to CONFIRMED automatically
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * GET /api/v1/payments/order/{orderId}
     * Get payment details for a specific order
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDto.PaymentResponse> getPaymentByOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    /**
     * GET /api/v1/payments/my
     * Get all payments for the authenticated customer
     */
    @GetMapping("/my")
    public ResponseEntity<List<PaymentDto.PaymentSummaryResponse>> getMyPayments(
            @AuthenticationPrincipal Jwt jwt) {

        UUID customerId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(paymentService.getCustomerPayments(customerId));
    }

    /**
     * POST /api/v1/payments/{orderId}/simulate-success
     *
     * DEV ONLY — simulates a successful Stripe payment confirmation.
     *
     * Postman test:
     * POST http://localhost:8080/api/v1/payments/{orderId}/simulate-success
     * Authorization: Bearer <any_valid_token>
     *
     * After calling this:
     * - Payment status → SUCCEEDED
     * - Kafka publishes "payment.succeeded"
     * - Order status → CONFIRMED (order-service consumes the event)
     * - Notification email sent (notification-service consumes the event)
     */
    @PostMapping("/{orderId}/simulate-success")
    public ResponseEntity<PaymentDto.PaymentResponse> simulateSuccess(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal Jwt jwt) {

        log.info("Simulating payment success for order: {}", orderId);
        return ResponseEntity.ok(paymentService.simulatePaymentSuccess(orderId));
    }

    /**
     * POST /api/v1/payments/{orderId}/simulate-failure
     * DEV ONLY — simulates a failed payment
     */
    @PostMapping("/{orderId}/simulate-failure")
    public ResponseEntity<PaymentDto.PaymentResponse> simulateFailure(
            @PathVariable UUID orderId,
            @RequestParam(defaultValue = "Card declined") String reason,
            @AuthenticationPrincipal Jwt jwt) {

        log.info("Simulating payment failure for order: {}", orderId);
        return ResponseEntity.ok(paymentService.simulatePaymentFailure(orderId, reason));
    }
    /**
     * POST /api/v1/payments/initiate
     * Frontend calls this after order is placed to get PayPal approval URL
     */
    @PostMapping("/initiate")
    public ResponseEntity<PaymentDto.PaymentResponse> initiatePayment(
            @RequestBody PaymentDto.InitiateRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        // Extract customerId from JWT subject
        request.setCustomerId(jwt.getSubject());

        return ResponseEntity.ok(
                paymentService.initiatePayment(
                        request.getOrderId(),
                        request.getAmount(),
                        request.getCustomerId()
                )
        );
    }

    /**
     * GET /api/v1/payments/success
     * PayPal redirects here after customer approves payment
     */
    @GetMapping("/success")
    public ResponseEntity<PaymentDto.PaymentResponse> paymentSuccess(
            @RequestParam String paymentId,
            @RequestParam String PayerID,
            @RequestParam String orderId) {
        return ResponseEntity.ok(
                paymentService.executePayment(paymentId, PayerID, orderId)
        );
    }

    /**
     * GET /api/v1/payments/cancel
     * PayPal redirects here if customer cancels
     */
    @GetMapping("/cancel")
    public ResponseEntity<Map<String, String>> paymentCancel(
            @RequestParam String orderId) {
        log.info("Payment cancelled for order: {}", orderId);
        return ResponseEntity.ok(Map.of(
                "message", "Payment cancelled",
                "orderId", orderId
        ));
    }
}
