package com.halalbite.paymentservice.dto;

import com.halalbite.paymentservice.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentDto {

    // =====================================================
    // RESPONSE DTOs
    // =====================================================

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PaymentResponse {
        private UUID id;
        private UUID orderId;
        private UUID customerId;
        private BigDecimal totalAmount;
        private BigDecimal platformFeeAmount;
        private BigDecimal restaurantAmount;
        private PaymentStatus status;
        // clientSecret is sent to the frontend to complete payment with Stripe.js
        // Never log or expose this unnecessarily
        private String clientSecret;
        private LocalDateTime paidAt;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PaymentSummaryResponse {
        private UUID id;
        private UUID orderId;
        private BigDecimal totalAmount;
        private PaymentStatus status;
        private LocalDateTime createdAt;
    }

    // =====================================================
    // KAFKA EVENT DTOs — consumed from order-service
    // =====================================================

    // Consumed from "order.placed" topic — triggers payment creation
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderPlacedEvent {
        private UUID orderId;
        private UUID customerId;
        private UUID restaurantId;
        private BigDecimal totalAmount;
        private BigDecimal platformFeeAmount;
        private String customerEmail;
        private LocalDateTime createdAt;
    }

    // =====================================================
    // KAFKA EVENT DTOs — published by payment-service
    // =====================================================

    // Published to "payment.succeeded" — order-service updates to CONFIRMED
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PaymentSucceededEvent {
        private UUID paymentId;
        private UUID orderId;
        private UUID customerId;
        private UUID restaurantId;
        private BigDecimal totalAmount;
        private BigDecimal platformFeeAmount;
        private BigDecimal restaurantAmount;
        private String stripePaymentIntentId;
        private LocalDateTime paidAt;
    }

    // Published to "payment.failed" — order-service updates to CANCELLED
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PaymentFailedEvent {
        private UUID paymentId;
        private UUID orderId;
        private UUID customerId;
        private String failureReason;
        private LocalDateTime failedAt;
    }
}
