package com.halalbite.orderservice.controller;

import com.halalbite.orderservice.dto.OrderDto;
import com.halalbite.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Order Controller
 *
 * Endpoints:
 *   POST   /api/v1/orders                          → place an order (CUSTOMER)
 *   GET    /api/v1/orders                          → my order history (CUSTOMER)
 *   GET    /api/v1/orders/{id}                     → get specific order (CUSTOMER)
 *   DELETE /api/v1/orders/{id}                     → cancel order (CUSTOMER)
 *   GET    /api/v1/orders/restaurant/{restaurantId} → restaurant's orders
 *   GET    /api/v1/orders/restaurant/{restaurantId}/active → live orders
 *   PATCH  /api/v1/orders/{id}/status              → update status (RESTAURANT)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/v1/orders
     * Place a new order — CUSTOMER only
     *
     * Postman body:
     * {
     *   "restaurantId": "uuid",
     *   "items": [
     *     { "menuItemId": "uuid", "quantity": 2, "specialRequests": "no onions" }
     *   ],
     *   "deliveryStreetAddress": "123 Main St",
     *   "deliveryCity": "Chicago",
     *   "deliveryPostalCode": "60601"
     * }
     */
    @PostMapping
    public ResponseEntity<OrderDto.OrderResponse> createOrder(
            @Valid @RequestBody OrderDto.CreateOrderRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID customerId = UUID.fromString(jwt.getSubject());
        log.info("POST /api/v1/orders — customer: {}", customerId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderService.createOrder(request, customerId));
    }

    /**
     * GET /api/v1/orders
     * Get my order history — CUSTOMER only
     */
    @GetMapping
    public ResponseEntity<Page<OrderDto.OrderSummaryResponse>> getMyOrders(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UUID customerId = UUID.fromString(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(orderService.getMyOrders(customerId, pageable));
    }

    /**
     * GET /api/v1/orders/{id}
     * Get a specific order — CUSTOMER only sees their own
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto.OrderResponse> getOrderById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        UUID customerId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(orderService.getOrderById(id, customerId));
    }
    /**
     * GET /api/v1/orders/admin/restaurant/{restaurantId}
     * Admin views all orders for any restaurant — no ownership check
     */
    @GetMapping("/admin/restaurant/{restaurantId}")
    public ResponseEntity<Page<OrderDto.OrderSummaryResponse>> getRestaurantOrdersForAdmin(
            @PathVariable UUID restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(orderService.getRestaurantOrders(restaurantId, pageable));
    }

    /**
     * DELETE /api/v1/orders/{id}
     * Cancel an order — CUSTOMER only, before PREPARING
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<OrderDto.OrderResponse> cancelOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        UUID customerId = UUID.fromString(jwt.getSubject());
        log.info("DELETE /api/v1/orders/{} — customer: {}", id, customerId);
        return ResponseEntity.ok(orderService.cancelOrder(id, customerId));
    }

    /**
     * GET /api/v1/orders/restaurant/{restaurantId}
     * Restaurant views all their orders
     */
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<Page<OrderDto.OrderSummaryResponse>> getRestaurantOrders(
            @PathVariable UUID restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(orderService.getRestaurantOrders(restaurantId, pageable));
    }

    /**
     * GET /api/v1/orders/restaurant/{restaurantId}/active
     * Get live active orders — for restaurant dashboard real-time view
     */
    @GetMapping("/restaurant/{restaurantId}/active")
    public ResponseEntity<List<OrderDto.OrderSummaryResponse>> getActiveOrders(
            @PathVariable UUID restaurantId) {

        return ResponseEntity.ok(orderService.getActiveRestaurantOrders(restaurantId));
    }

    /**
     * PATCH /api/v1/orders/{id}/status
     * Update order status — RESTAURANT_OWNER
     *
     * Body: { "status": "PREPARING", "reason": "optional note" }
     */
//    @PatchMapping("/{id}/order/status")
//    public ResponseEntity<OrderDto.OrderResponse> updateStatus(
//            @PathVariable UUID id,
//            @Valid @RequestBody OrderDto.UpdateStatusRequest request,
//            @AuthenticationPrincipal Jwt jwt) {
//
//        // TODO: Extract restaurantId from restaurant-service using owner's JWT sub
//        // For now using a placeholder — restaurant service call needed
//        UUID restaurantId = UUID.fromString(jwt.getClaim("restaurantId") != null
//            ? jwt.getClaim("restaurantId").toString()
//            : "00000000-0000-0000-0000-000000000000");
//
//        log.info("PATCH /api/v1/orders/{}/status → {}", id, request.getStatus());
//        return ResponseEntity.ok
//           (orderService.updateOrderStatus(id, request, restaurantId));
//    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDto.OrderResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody OrderDto.UpdateStatusRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        // Get the order first to find restaurantId — no longer need it from JWT
        UUID ownerUserId = UUID.fromString(jwt.getSubject());
        log.info("PATCH /api/v1/orders/{}/status → {} by owner: {}", id, request.getStatus(), ownerUserId);
        return ResponseEntity.ok(
                orderService.updateOrderStatusByOwner(id, request, ownerUserId)
        );
    }


    /**
     * GET /api/v1/orders/restaurant-view/{id}
     * Restaurant owner views a specific order — no customerId check
     */
    @GetMapping("/restaurant-view/{id}")
    public ResponseEntity<OrderDto.OrderResponse> getOrderForRestaurant(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(orderService.getOrderForRestaurant(id));
    }

}
