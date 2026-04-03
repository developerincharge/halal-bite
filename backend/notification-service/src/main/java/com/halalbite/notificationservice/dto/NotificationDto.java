package com.halalbite.notificationservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTOs that mirror the event payloads published by other services.
 * These must match the structure of:
 *   - order-service OrderDto.OrderPlacedEvent
 *   - order-service OrderDto.OrderStatusChangedEvent
 *   - payment-service PaymentDto.PaymentSucceededEvent
 *   - payment-service PaymentDto.PaymentFailedEvent
 */
public class NotificationDto {

    // Consumed from "order.placed" topic
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

    // Consumed from "order.status.updated" topic
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderStatusChangedEvent {
        private UUID orderId;
        private UUID customerId;
        private UUID restaurantId;
        private String oldStatus;
        private String newStatus;
        private LocalDateTime updatedAt;
    }

    // Consumed from "payment.succeeded" topic
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

    // Consumed from "payment.failed" topic
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PaymentFailedEvent {
        private UUID paymentId;
        private UUID orderId;
        private UUID customerId;
        private String failureReason;
        private LocalDateTime failedAt;
    }
}
