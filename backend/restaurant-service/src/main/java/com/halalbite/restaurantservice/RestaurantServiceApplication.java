package com.halalbite.restaurantservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * HALAL-BITE Restaurant Service
 *
 * Responsibilities:
 * - Restaurant profile management (name, address, cuisine type, logo)
 * - Affiliate revenue configuration (what % the restaurant pays halal-bite)
 * - Restaurant status management (PENDING, ACTIVE, SUSPENDED, CLOSED)
 * - Operating hours management
 *
 * This service owns the restaurant_service_db database.
 * The menu-service calls this service to validate a restaurant exists
 * before creating menu items for it.
 *
 * Port: 8083
 * Base URL (via gateway): http://localhost:8080/api/v1/restaurants
 *
 * User roles:
 *   RESTAURANT_OWNER — manages their own restaurant
 *   ADMIN            — manages all restaurants, approves new ones
 *   CUSTOMER         — can only read restaurant data (public endpoints)
 */
@SpringBootApplication
public class RestaurantServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }
}
