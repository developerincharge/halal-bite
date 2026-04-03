package com.halalbite.paymentservice.service;

import com.halalbite.paymentservice.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumer — listens to "order.placed" topic
 *
 * When order-service publishes an order.placed event,
 * this consumer automatically triggers payment creation.
 *
 * This is the event-driven integration between order-service
 * and payment-service — they never call each other directly.
 *
 * groupId "payment-service" ensures this consumer group
 * gets each message exactly once even if multiple instances
 * of payment-service are running.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPlacedKafkaConsumer {

    private final PaymentService paymentService;

    @KafkaListener(
        topics = "order.placed",
        groupId = "payment-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderPlaced(PaymentDto.OrderPlacedEvent event) {
        log.info("Received order.placed event for order: {} amount: ${}",
            event.getOrderId(), event.getTotalAmount());

        try {
            paymentService.createPayment(event);
            log.info("Payment created for order: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to create payment for order: {}", event.getOrderId(), e);
            // TODO: Send to dead letter queue for retry
        }
    }
}
