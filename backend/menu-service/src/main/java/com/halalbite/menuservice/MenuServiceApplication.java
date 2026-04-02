package com.halalbite.menuservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * HALAL-BITE Menu Service
 *
 * Responsibilities:
 * - Food category management per restaurant (Burgers, Sides, Drinks etc.)
 * - Menu item management (name, description, price, image, dietary flags)
 * - Item availability toggling (mark items as out of stock instantly)
 * - Special pricing (happy hour, deals, discounts)
 *
 * Relationship to other services:
 * - Validates restaurantId exists by calling restaurant-service
 *   before creating a menu item (service-to-service call)
 * - order-service calls this service to get item prices when
 *   a customer places an order
 *
 * Port: 8084
 * Base URL (via gateway): http://localhost:8080/api/v1/menus
 *
 * Data model:
 *   Restaurant (in restaurant-service)
 *     └── MenuCategory (Burgers, Drinks, Sides)
 *           └── MenuItem (Big Halal Burger, $12.99)
 */
@SpringBootApplication
public class MenuServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MenuServiceApplication.class, args);
    }
}
