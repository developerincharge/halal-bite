package com.halalbite.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * HALAL-BITE API Gateway
 *
 * What this does:
 * - Acts as the single entry point for ALL frontend requests
 * - Routes each request to the correct microservice based on the URL path
 * - Validates JWT tokens before forwarding any request
 * - Handles cross-cutting concerns: CORS, logging, rate limiting
 *
 * How routing works:
 *   Customer app calls  → http://localhost:8080/api/v1/restaurants/...
 *   Gateway checks path → routes to restaurant-service (port 8083)
 *   restaurant-service  → processes request and returns response
 *   Gateway             → forwards response back to customer app
 *
 * The frontend apps NEVER talk directly to individual microservices.
 * They only talk to this gateway. This is a core security principle.
 *
 * Note: @EnableEurekaClient is not needed in Spring Boot 3 —
 * it auto-configures when the dependency is on the classpath.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
