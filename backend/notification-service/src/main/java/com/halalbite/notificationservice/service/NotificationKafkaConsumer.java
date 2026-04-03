package com.halalbite.notificationservice.service;

import com.halalbite.notificationservice.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumers for notification-service
 *
 * Listens to 4 topics and triggers the appropriate email for each.
 *
 * Why a single class for all consumers?
 * Simpler to see all notification triggers in one place.
 * Each @KafkaListener method handles one specific event type.
 *
 * groupId "notification-service" ensures each instance of this
 * service gets each message exactly once even when scaled horizontally.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private final EmailService emailService;

    /**
     * Order placed → send order confirmation to customer
     * Triggered immediately when customer places an order
     */
    @KafkaListener(
        topics = "order.placed",
        groupId = "notification-service-orders",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderPlaced(NotificationDto.OrderPlacedEvent event) {
        log.info("Received order.placed — sending confirmation for order: {}", event.getOrderId());
        try {
            emailService.sendOrderConfirmation(event);
        } catch (Exception e) {
            log.error("Failed to send order confirmation for: {}", event.getOrderId(), e);
        }
    }

    /**
     * Payment succeeded → send payment receipt to customer
     * Triggered after payment-service confirms payment
     */
    @KafkaListener(
        topics = "payment.succeeded",
        groupId = "notification-service-payments",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentSucceeded(NotificationDto.PaymentSucceededEvent event) {
        log.info("Received payment.succeeded — sending receipt for order: {}", event.getOrderId());
        try {
            emailService.sendPaymentReceipt(event);
        } catch (Exception e) {
            log.error("Failed to send payment receipt for order: {}", event.getOrderId(), e);
        }
    }

    /**
     * Payment failed → send failure alert to customer
     * Triggered when Stripe declines the payment
     */
    @KafkaListener(
        topics = "payment.failed",
        groupId = "notification-service-payment-failures",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(NotificationDto.PaymentFailedEvent event) {
        log.info("Received payment.failed — sending alert for order: {}", event.getOrderId());
        try {
            emailService.sendPaymentFailureAlert(event);
        } catch (Exception e) {
            log.error("Failed to send payment failure alert for order: {}", event.getOrderId(), e);
        }
    }

    /**
     * Order status changed → send status update to customer
     * Triggered each time restaurant updates the order status
     */
    @KafkaListener(
        topics = "order.status.updated",
        groupId = "notification-service-status",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderStatusChanged(NotificationDto.OrderStatusChangedEvent event) {
        log.info("Received order.status.updated — {} → {} for order: {}",
            event.getOldStatus(), event.getNewStatus(), event.getOrderId());
        try {
            emailService.sendOrderStatusUpdate(event);
        } catch (Exception e) {
            log.error("Failed to send status update for order: {}", event.getOrderId(), e);
        }
    }
}
