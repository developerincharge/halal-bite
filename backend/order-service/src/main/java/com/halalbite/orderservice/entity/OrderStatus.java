package com.halalbite.orderservice.entity;

/**
 * OrderStatus — the complete lifecycle of an order
 *
 * PENDING    → order created, awaiting payment confirmation
 * CONFIRMED  → payment received, restaurant notified
 * PREPARING  → restaurant is actively preparing the food
 * READY      → food is ready, waiting for pickup/delivery
 * DELIVERED  → order completed successfully
 * CANCELLED  → order cancelled (only allowed before PREPARING)
 *
 * Valid transitions:
 *   PENDING    → CONFIRMED  (payment-service confirms payment)
 *   PENDING    → CANCELLED  (customer cancels before payment)
 *   CONFIRMED  → PREPARING  (restaurant accepts the order)
 *   CONFIRMED  → CANCELLED  (restaurant rejects — rare)
 *   PREPARING  → READY      (restaurant marks food as ready)
 *   READY      → DELIVERED  (delivery confirmed)
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY,
    DELIVERED,
    CANCELLED
}
