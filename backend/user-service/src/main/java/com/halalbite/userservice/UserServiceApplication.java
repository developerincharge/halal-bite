package com.halalbite.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * HALAL-BITE User Service
 *
 * Responsibilities:
 * - User registration and profile management
 * - Delivery address management (a user can have multiple addresses)
 * - User preferences (dietary requirements, favourite cuisines)
 *
 * This service owns the user_service_db database.
 * NO other service reads this database directly.
 * If order-service needs a user's name, it calls this service's API.
 *
 * Port: 8082
 * Base URL (via gateway): http://localhost:8080/api/v1/users
 */
@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
