package com.halalbite.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * HALAL-BITE Order Service
 *
 * This is the most complex service — it orchestrates across
 * multiple other services to complete an order.
 *
 * Responsibilities:
 * - Create orders from a customer's cart
 * - Validate menu items exist and are available (calls menu-service)
 * - Calculate order total including platform fees
 * - Track order through its full lifecycle
 * - Publish events to Kafka for payment and notifications
 *
 * Order lifecycle:
 *   PENDING     → order created, awaiting payment
 *   CONFIRMED   → payment successful, sent to restaurant
 *   PREPARING   → restaurant acknowledged and is preparing
 *   READY       → food is ready for pickup/delivery
 *   DELIVERED   → customer received their order
 *   CANCELLED   → order was cancelled (before PREPARING)
 *
 * Service-to-service calls:
 *   → menu-service:   validate items and get current prices
 *
 * Kafka events published:
 *   → order.placed:          triggers payment-service
 *   → order.confirmed:       triggers notification to customer
 *   → order.status.updated:  triggers notification on every status change
 *
 * Port: 8085
 */
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
