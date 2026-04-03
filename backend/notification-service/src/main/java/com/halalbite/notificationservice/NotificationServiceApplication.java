package com.halalbite.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * HALAL-BITE Notification Service
 *
 * The last piece of the backend puzzle.
 * Listens to Kafka events and sends emails to customers and restaurants.
 *
 * Events consumed:
 *   "order.placed"          → send order confirmation email to customer
 *   "payment.succeeded"     → send payment receipt email to customer
 *   "payment.failed"        → send payment failure alert to customer
 *   "order.status.updated"  → send status update email (preparing, ready, delivered)
 *
 * Email provider:
 *   Development: Mailtrap (fake inbox — no real emails sent)
 *   Production:  Configure SMTP credentials for real email provider
 *
 * All sent notifications are logged to notification_service_db
 * for audit trail and debugging.
 *
 * Port: 8087
 */
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
