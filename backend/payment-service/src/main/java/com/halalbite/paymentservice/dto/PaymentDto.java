package com.halalbite.paymentservice.dto;

import com.halalbite.paymentservice.entity.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderPlacedEvent {
        private String orderId;
        private BigDecimal totalAmount;
        private String customerId;
        private String restaurantId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentResponse {
        private UUID id;
        private UUID orderId;
        private BigDecimal amount;
        private String currency;
        private PaymentStatus status;
        private String approvalUrl;
        private String paypalPaymentId;
        private LocalDateTime createdAt;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentSummaryResponse {
        private UUID id;
        private UUID orderId;
        private BigDecimal amount;
        private PaymentStatus status;
        private LocalDateTime createdAt;
    }



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitiateRequest {
        private String orderId;
        private BigDecimal amount;
        private String customerId;
    }
}