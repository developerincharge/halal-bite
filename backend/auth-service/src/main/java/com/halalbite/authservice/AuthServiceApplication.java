package com.halalbite.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * HALAL-BITE Auth Service
 *
 * Replaces Keycloak with a simple, self-contained auth system.
 *
 * What this service does:
 * - Registers new users (stores hashed password in auth_service_db)
 * - Authenticates users (login with email + password)
 * - Issues JWT tokens that ALL other services can validate
 * - Refreshes expired tokens
 *
 * How it works:
 * 1. User registers → POST /api/v1/auth/register
 * 2. User logs in → POST /api/v1/auth/login → receives JWT token
 * 3. User sends JWT in every request header: "Authorization: Bearer <token>"
 * 4. Other services (user-service, restaurant-service etc.) validate the token
 *    using the shared secret key in application.yml
 *
 * JWT token contains:
 *   sub   → userId (UUID)
 *   email → user's email
 *   roles → ["CUSTOMER"] or ["RESTAURANT_OWNER"] or ["ADMIN"]
 *   iat   → issued at timestamp
 *   exp   → expiry timestamp (24 hours)
 *
 * Port: 8081
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
