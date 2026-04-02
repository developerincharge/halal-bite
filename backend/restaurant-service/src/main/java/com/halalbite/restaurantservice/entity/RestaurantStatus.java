package com.halalbite.restaurantservice.entity;

/**
 * Restaurant Status — lifecycle of a restaurant on halal-bite
 *
 * PENDING   → restaurant just registered, admin must approve
 * ACTIVE    → visible to customers, can receive orders
 * SUSPENDED → hidden from customers (e.g. compliance issue)
 * CLOSED    → permanently closed, soft deleted
 *
 * State transitions (admin controls these):
 *   PENDING → ACTIVE    (admin approves)
 *   ACTIVE  → SUSPENDED (admin suspends)
 *   SUSPENDED → ACTIVE  (admin reinstates)
 *   Any → CLOSED        (admin closes permanently)
 */
public enum RestaurantStatus {
    PENDING,
    ACTIVE,
    SUSPENDED,
    CLOSED
}
