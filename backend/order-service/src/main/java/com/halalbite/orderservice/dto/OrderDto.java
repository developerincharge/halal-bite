package com.halalbite.orderservice.dto;

import com.halalbite.orderservice.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderDto {

    // =====================================================
    // REQUEST DTOs
    // =====================================================

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateOrderRequest {

        @NotNull(message = "Restaurant ID is required")
        private UUID restaurantId;

        @NotEmpty(message = "Order must have at least one item")
        @Valid
        private List<OrderLineItemRequest> items;

        // Delivery address — required for delivery orders
        @NotBlank(message = "Delivery street address is required")
        private String deliveryStreetAddress;

        @NotBlank(message = "Delivery city is required")
        private String deliveryCity;

        private String deliveryState;

        @NotBlank(message = "Delivery postal code is required")
        private String deliveryPostalCode;

        private String specialInstructions;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderLineItemRequest {

        @NotNull(message = "Menu item ID is required")
        private UUID menuItemId;

        @NotNull @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 20, message = "Cannot order more than 20 of one item")
        private Integer quantity;

        private String specialRequests;  // e.g. "extra spicy, no onions"
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateStatusRequest {

        @NotNull(message = "Status is required")
        private OrderStatus status;

        private String reason;
    }

    // =====================================================
    // RESPONSE DTOs
    // =====================================================

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderResponse {
        private UUID id;
        private UUID customerId;
        private UUID restaurantId;
        private List<LineItemResponse> lineItems;
        private BigDecimal subtotal;
        private BigDecimal deliveryFee;
        private BigDecimal platformFeeAmount;
        private BigDecimal totalAmount;
        private String deliveryStreetAddress;
        private String deliveryCity;
        private String deliveryState;
        private String deliveryPostalCode;
        private String specialInstructions;
        private OrderStatus status;
        private String paymentIntentId;
        private LocalDateTime estimatedReadyAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LineItemResponse {
        private UUID id;
        private UUID menuItemId;
        private String itemName;
        private BigDecimal itemUnitPrice;
        private Integer quantity;
        private BigDecimal lineTotal;
        private String specialRequests;
    }

    // Lightweight summary for order history lists
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderSummaryResponse {
        private UUID id;
        private UUID restaurantId;
        private BigDecimal totalAmount;
        private Integer itemCount;
        private OrderStatus status;
        private LocalDateTime createdAt;
    }

    // Kafka event payload — published when order is placed
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

    // Kafka event payload — published on every status change
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderStatusChangedEvent {
        private UUID orderId;
        private UUID customerId;
        private UUID restaurantId;
        private OrderStatus oldStatus;
        private OrderStatus newStatus;
        private LocalDateTime updatedAt;
    }
}
