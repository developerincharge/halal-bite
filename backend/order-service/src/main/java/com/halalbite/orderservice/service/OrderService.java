package com.halalbite.orderservice.service;

import com.halalbite.orderservice.dto.OrderDto;
import com.halalbite.orderservice.entity.Order;
import com.halalbite.orderservice.entity.OrderLineItem;
import com.halalbite.orderservice.entity.OrderStatus;
import com.halalbite.orderservice.exception.OrderExceptions;
import com.halalbite.orderservice.mapper.OrderMapper;
import com.halalbite.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Order Service — the heart of halal-bite
 *
 * createOrder() flow:
 * 1. For each item in the request:
 *    a. Call menu-service to get current price and availability
 *    b. Validate item belongs to the requested restaurant
 *    c. Create an OrderLineItem with snapshotted price
 * 2. Calculate subtotal, platform fee, and total
 * 3. Save order to database
 * 4. Publish "order.placed" event to Kafka
 *    → payment-service listens and creates a Stripe payment intent
 *    → notification-service listens and sends confirmation email
 *
 * Platform fee calculation:
 *   Default 15% (0.15) — this should come from restaurant profile
 *   TODO: Fetch actual affiliate % from restaurant-service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final BigDecimal DEFAULT_PLATFORM_FEE_RATE = new BigDecimal("0.15");
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("2.99");

    private final OrderRepository orderRepository;
    private final MenuServiceClient menuServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderMapper orderMapper;

    /**
     * Place a new order.
     * Validates all items, calculates totals, saves, and publishes Kafka event.
     */
    @Transactional
    public OrderDto.OrderResponse createOrder(
            OrderDto.CreateOrderRequest request,
            UUID customerId) {

        log.info("Creating order for customer: {} at restaurant: {}",
            customerId, request.getRestaurantId());

        Order order = Order.builder()
            .customerId(customerId)
            .restaurantId(request.getRestaurantId())
            .deliveryStreetAddress(request.getDeliveryStreetAddress())
            .deliveryCity(request.getDeliveryCity())
            .deliveryState(request.getDeliveryState())
            .deliveryPostalCode(request.getDeliveryPostalCode())
            .specialInstructions(request.getSpecialInstructions())
            .status(OrderStatus.PENDING)
            .estimatedReadyAt(LocalDateTime.now().plusMinutes(45))
            .build();

        // Build line items by calling menu-service for each item
        List<OrderLineItem> lineItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderDto.OrderLineItemRequest itemRequest : request.getItems()) {

            // Call menu-service to get current price and availability
            MenuServiceClient.ItemPriceResponse menuItem;
            try {
                menuItem = menuServiceClient.getItemPrice(itemRequest.getMenuItemId());
            } catch (Exception e) {
                throw new OrderExceptions.MenuItemUnavailableException(
                    "Menu item not found: " + itemRequest.getMenuItemId()
                );
            }

            // Validate item is available
            if (!Boolean.TRUE.equals(menuItem.getIsAvailable())) {
                throw new OrderExceptions.MenuItemUnavailableException(
                    "Item '" + menuItem.getName() + "' is currently unavailable"
                );
            }

            // Validate item belongs to the requested restaurant
            if (!menuItem.getRestaurantId().equals(request.getRestaurantId())) {
                throw new OrderExceptions.MenuItemUnavailableException(
                    "Item does not belong to the requested restaurant"
                );
            }

            // Snapshot the price at order time
            BigDecimal effectivePrice = menuItem.getEffectivePrice();
            BigDecimal lineTotal = effectivePrice
                .multiply(new BigDecimal(itemRequest.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);

            OrderLineItem lineItem = OrderLineItem.builder()
                .order(order)
                .menuItemId(itemRequest.getMenuItemId())
                .itemName(menuItem.getName())
                .itemUnitPrice(effectivePrice)
                .quantity(itemRequest.getQuantity())
                .lineTotal(lineTotal)
                .specialRequests(itemRequest.getSpecialRequests())
                .build();

            lineItems.add(lineItem);
            subtotal = subtotal.add(lineTotal);
        }

        order.setLineItems(lineItems);
        order.setSubtotal(subtotal);
        order.setDeliveryFee(DELIVERY_FEE);

        // Calculate platform fee (halal-bite's revenue share)
        BigDecimal platformFee = subtotal
            .multiply(DEFAULT_PLATFORM_FEE_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        order.setPlatformFeeAmount(platformFee);

        // Total = subtotal + delivery fee (platform fee is taken from subtotal, not added)
        BigDecimal total = subtotal.add(DELIVERY_FEE).setScale(2, RoundingMode.HALF_UP);
        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created: {} total: ${}", savedOrder.getId(), total);

        // Publish Kafka event — triggers payment and notification services
        publishOrderPlacedEvent(savedOrder);

        return orderMapper.toResponse(savedOrder);
    }

    /**
     * Get a specific order — customer can only see their own orders.
     */
    @Transactional(readOnly = true)
    public OrderDto.OrderResponse getOrderById(UUID orderId, UUID customerId) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, customerId)
            .orElseThrow(() -> new OrderExceptions.OrderNotFoundException(
                "Order not found: " + orderId
            ));
        return orderMapper.toResponse(order);
    }

    /**
     * Get customer's order history — paginated.
     */
    @Transactional(readOnly = true)
    public Page<OrderDto.OrderSummaryResponse> getMyOrders(UUID customerId, Pageable pageable) {
        return orderRepository
            .findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
            .map(orderMapper::toSummaryResponse);
    }

    /**
     * Get restaurant's orders — for restaurant dashboard.
     */
    @Transactional(readOnly = true)
    public Page<OrderDto.OrderSummaryResponse> getRestaurantOrders(
            UUID restaurantId, Pageable pageable) {
        return orderRepository
            .findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable)
            .map(orderMapper::toSummaryResponse);
    }

    /**
     * Get restaurant's active orders (not delivered or cancelled).
     * Used by the restaurant dashboard to show live orders.
     */
    @Transactional(readOnly = true)
    public List<OrderDto.OrderSummaryResponse> getActiveRestaurantOrders(UUID restaurantId) {
        List<OrderStatus> activeStatuses = List.of(
            OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING
        );
        return orderMapper.toSummaryResponseList(
            orderRepository.findByRestaurantIdAndStatusIn(restaurantId, activeStatuses)
        );
    }

    /**
     * Update order status — used by restaurant to progress the order.
     * Validates the transition is allowed before saving.
     */
    @Transactional
    public OrderDto.OrderResponse updateOrderStatus(
            UUID orderId,
            OrderDto.UpdateStatusRequest request,
            UUID restaurantId) {

        Order order = orderRepository.findByIdAndRestaurantId(orderId, restaurantId)
            .orElseThrow(() -> new OrderExceptions.OrderNotFoundException(
                "Order not found: " + orderId
            ));

        validateStatusTransition(order.getStatus(), request.getStatus());

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getStatus());
        Order updated = orderRepository.save(order);

        // Publish status change event to notify customer
        publishStatusChangedEvent(updated, oldStatus);

        log.info("Order {} status updated: {} → {}", orderId, oldStatus, request.getStatus());
        return orderMapper.toResponse(updated);
    }

    /**
     * Cancel an order — only allowed if PENDING or CONFIRMED.
     */
    @Transactional
    public OrderDto.OrderResponse cancelOrder(UUID orderId, UUID customerId) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, customerId)
            .orElseThrow(() -> new OrderExceptions.OrderNotFoundException(
                "Order not found: " + orderId
            ));

        if (order.getStatus() == OrderStatus.PREPARING
                || order.getStatus() == OrderStatus.READY
                || order.getStatus() == OrderStatus.DELIVERED) {
            throw new OrderExceptions.OrderNotCancellableException(
                "Cannot cancel order in status: " + order.getStatus()
                + ". Order can only be cancelled before preparation begins."
            );
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        Order updated = orderRepository.save(order);
        publishStatusChangedEvent(updated, oldStatus);

        log.info("Order {} cancelled by customer: {}", orderId, customerId);
        return orderMapper.toResponse(updated);
    }

    // =====================================================
    // Private helpers
    // =====================================================

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING    -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED  -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
            case PREPARING  -> next == OrderStatus.READY;
            case READY      -> next == OrderStatus.DELIVERED;
            default         -> false;
        };

        if (!valid) {
            throw new OrderExceptions.InvalidOrderStatusException(
                "Invalid status transition: " + current + " → " + next
            );
        }
    }

    private void publishOrderPlacedEvent(Order order) {
        try {
            OrderDto.OrderPlacedEvent event = OrderDto.OrderPlacedEvent.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .restaurantId(order.getRestaurantId())
                .totalAmount(order.getTotalAmount())
                .platformFeeAmount(order.getPlatformFeeAmount())
                .createdAt(order.getCreatedAt())
                .build();

            kafkaTemplate.send("order.placed", order.getId().toString(), event);
            log.info("Published order.placed event for order: {}", order.getId());
        } catch (Exception e) {
            // TODO: Implement outbox pattern for guaranteed delivery
            log.error("Failed to publish order.placed event for order: {}", order.getId(), e);
        }
    }

    private void publishStatusChangedEvent(Order order, OrderStatus oldStatus) {
        try {
            OrderDto.OrderStatusChangedEvent event = OrderDto.OrderStatusChangedEvent.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .restaurantId(order.getRestaurantId())
                .oldStatus(oldStatus)
                .newStatus(order.getStatus())
                .updatedAt(order.getUpdatedAt())
                .build();

            kafkaTemplate.send("order.status.updated", order.getId().toString(), event);
            log.info("Published order.status.updated event: {} → {}",
                oldStatus, order.getStatus());
        } catch (Exception e) {
            log.error("Failed to publish status event for order: {}", order.getId(), e);
        }
    }
}
