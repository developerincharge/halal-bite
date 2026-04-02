package com.halalbite.authservice.entity;

/**
 * UserRole — the 3 roles in the halal-bite platform
 *
 * These are embedded in every JWT token as the "roles" claim.
 * Other microservices read this claim to enforce permissions.
 *
 * CUSTOMER         → can order food, manage own profile
 * RESTAURANT_OWNER → can manage their restaurant, menus, view orders
 * ADMIN            → full platform access, approve restaurants
 */
public enum UserRole {
    CUSTOMER,
    RESTAURANT_OWNER,
    ADMIN
}
