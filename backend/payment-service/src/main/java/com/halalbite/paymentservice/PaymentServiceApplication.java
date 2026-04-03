package com.halalbite.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * HALAL-BITE Payment Service
 *
 * Handles all financial transactions using Stripe Connect.
 *
 * How the affiliate payment model works:
 * 1. Customer pays full order total (e.g. $15.98)
 * 2. Stripe splits the payment automatically:
 *    - Restaurant receives: $15.98 - $2.40 (15% of $12.99) = $13.58
 *    - Halal-Bite platform receives: $2.40 platform fee
 *    - Delivery fee ($2.99) goes to the platform too
 *
 * Flow triggered by Kafka:
 *   order-service publishes → "order.placed"
 *   payment-service consumes → creates Stripe PaymentIntent
 *   Customer pays via frontend → Stripe webhook fires
 *   payment-service receives webhook → confirms payment
 *   payment-service publishes → "payment.succeeded" or "payment.failed"
 *   order-service consumes → updates order to CONFIRMED or CANCELLED
 *
 * For development (no real Stripe keys):
 * We simulate payment confirmation without calling Stripe.
 * Use POST /api/v1/payments/{orderId}/simulate-success in Postman.
 *
 * Port: 8086
 */
@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
