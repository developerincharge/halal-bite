// backend/order-service/src/main/java/com/halalbite/orderservice/kafka/PaymentEventConsumer.java

package com.halalbite.orderservice.kafka;

import com.halalbite.orderservice.entity.OrderStatus;
import com.halalbite.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Listens for payment events from payment-service via Kafka.
 *
 * Flow:
 * payment.succeeded → order status → CONFIRMED
 * payment.failed    → order status → CANCELLED
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;

    @KafkaListener(
        topics = "payment.succeeded",
        groupId = "order-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentSucceeded(Map<String, Object> event) {
        log.info("Received payment.succeeded event: {}", event);

        try {
            String orderId = (String) event.get("orderId");
            if (orderId == null) {
                log.error("No orderId in payment.succeeded event — skipping");
                return;
            }
            orderService.confirmOrder(UUID.fromString(orderId));
            log.info("Order {} confirmed after payment succeeded", orderId);
        } catch (Exception e) {
            log.error("Failed to process payment.succeeded: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
        topics = "payment.failed",
        groupId = "order-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(Map<String, Object> event) {
        log.info("Received payment.failed event: {}", event);

        try {
            String orderId = (String) event.get("orderId");
            if (orderId == null) {
                log.error("No orderId in payment.failed event — skipping");
                return;
            }
            orderService.cancelOrder(UUID.fromString(orderId));
            log.info("Order {} cancelled after payment failed", orderId);
        } catch (Exception e) {
            log.error("Failed to process payment.failed: {}", e.getMessage(), e);
        }
    }
}