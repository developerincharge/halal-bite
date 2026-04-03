package com.halalbite.notificationservice.service;

import com.halalbite.notificationservice.dto.NotificationDto;
import com.halalbite.notificationservice.entity.NotificationLog;
import com.halalbite.notificationservice.entity.NotificationStatus;
import com.halalbite.notificationservice.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Email Service — sends emails and logs every attempt
 *
 * Uses SimpleMailMessage for plain text emails.
 * In production you'd use MimeMessage with HTML templates via Thymeleaf.
 *
 * For local dev with Mailtrap:
 * All emails are intercepted by Mailtrap — nothing reaches real inboxes.
 * Check your Mailtrap inbox at mailtrap.io to see all sent emails.
 *
 * The service falls back gracefully if email config is missing —
 * logs the notification as SKIPPED so the app keeps running.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final NotificationLogRepository notificationLogRepository;

    private static final String FROM_EMAIL = "noreply@halalbite.com";

    /**
     * Send order placed confirmation to customer.
     */
    public void sendOrderConfirmation(NotificationDto.OrderPlacedEvent event) {
        String subject = "Order Confirmed — Halal Bite #" + shortId(event.getOrderId());
        String body = buildOrderConfirmationBody(event);
        String recipientEmail = getEmailForCustomer(event.getCustomerId(), event.getCustomerEmail());

        sendEmail(
            recipientEmail,
            subject,
            body,
            "ORDER_CONFIRMED",
            event.getCustomerId(),
            event.getOrderId()
        );
    }

    /**
     * Send payment receipt to customer.
     */
    public void sendPaymentReceipt(NotificationDto.PaymentSucceededEvent event) {
        String subject = "Payment Receipt — Halal Bite #" + shortId(event.getOrderId());
        String body = buildPaymentReceiptBody(event);

        // TODO: Fetch customer email from user-service via Feign
        // For now using a placeholder email
        String recipientEmail = "customer@halalbite.com";

        sendEmail(
            recipientEmail,
            subject,
            body,
            "PAYMENT_RECEIPT",
            event.getCustomerId(),
            event.getOrderId()
        );
    }

    /**
     * Send payment failure alert to customer.
     */
    public void sendPaymentFailureAlert(NotificationDto.PaymentFailedEvent event) {
        String subject = "Payment Failed — Halal Bite Order #" + shortId(event.getOrderId());
        String body = buildPaymentFailureBody(event);
        String recipientEmail = "customer@halalbite.com";

        sendEmail(
            recipientEmail,
            subject,
            body,
            "PAYMENT_FAILED",
            event.getCustomerId(),
            event.getOrderId()
        );
    }

    /**
     * Send order status update to customer.
     */
    public void sendOrderStatusUpdate(NotificationDto.OrderStatusChangedEvent event) {
        // Only notify customer for meaningful status changes
        if (event.getNewStatus() == null) return;

        String subject = buildStatusSubject(event);
        String body = buildStatusUpdateBody(event);
        String recipientEmail = "customer@halalbite.com";

        sendEmail(
            recipientEmail,
            subject,
            body,
            "ORDER_STATUS_" + event.getNewStatus(),
            event.getCustomerId(),
            event.getOrderId()
        );
    }

    // =====================================================
    // Private helpers
    // =====================================================

    private void sendEmail(
            String to,
            String subject,
            String body,
            String notificationType,
            UUID recipientId,
            UUID referenceId) {

        NotificationLog log = NotificationLog.builder()
            .recipientId(recipientId)
            .recipientEmail(to)
            .notificationType(notificationType)
            .subject(subject)
            .referenceId(referenceId)
            .build();

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.setStatus(NotificationStatus.SENT);
            this.log.info("Email sent — type: {} to: {}", notificationType, to);

        } catch (Exception e) {
            log.setStatus(NotificationStatus.FAILED);
            log.setErrorMessage(e.getMessage());
            this.log.error("Failed to send email — type: {} error: {}", notificationType, e.getMessage());
        } finally {
            notificationLogRepository.save(log);
        }
    }

    private String buildOrderConfirmationBody(NotificationDto.OrderPlacedEvent event) {
        return """
            Thank you for your order with Halal Bite!

            Order ID: %s
            Total Amount: $%s

            Your order has been received and is awaiting payment confirmation.
            You will receive another email once your payment is processed.

            Thank you for choosing Halal Bite!
            The Halal Bite Team
            """.formatted(event.getOrderId(), event.getTotalAmount());
    }

    private String buildPaymentReceiptBody(NotificationDto.PaymentSucceededEvent event) {
        return """
            Payment Successful!

            Order ID: %s
            Amount Paid: $%s

            Your payment has been confirmed and your order is now being prepared.
            We'll notify you when your order is ready.

            Thank you for choosing Halal Bite!
            The Halal Bite Team
            """.formatted(event.getOrderId(), event.getTotalAmount());
    }

    private String buildPaymentFailureBody(NotificationDto.PaymentFailedEvent event) {
        return """
            Payment Failed

            Order ID: %s
            Reason: %s

            Unfortunately your payment could not be processed.
            Your order has been cancelled. Please try again with a different payment method.

            Need help? Contact us at support@halalbite.com

            The Halal Bite Team
            """.formatted(event.getOrderId(), event.getFailureReason());
    }

    private String buildStatusUpdateBody(NotificationDto.OrderStatusChangedEvent event) {
        String statusMessage = switch (event.getNewStatus()) {
            case "CONFIRMED"  -> "Your order has been confirmed and sent to the restaurant!";
            case "PREPARING"  -> "The restaurant is now preparing your order.";
            case "READY"      -> "Your order is ready! The delivery is on its way.";
            case "DELIVERED"  -> "Your order has been delivered. Enjoy your meal!";
            case "CANCELLED"  -> "Your order has been cancelled.";
            default           -> "Your order status has been updated to: " + event.getNewStatus();
        };

        return """
            Order Update — Halal Bite

            Order ID: %s
            Status: %s

            %s

            Thank you for choosing Halal Bite!
            The Halal Bite Team
            """.formatted(event.getOrderId(), event.getNewStatus(), statusMessage);
    }

    private String buildStatusSubject(NotificationDto.OrderStatusChangedEvent event) {
        return switch (event.getNewStatus()) {
            case "CONFIRMED" -> "Order Confirmed — Halal Bite #" + shortId(event.getOrderId());
            case "PREPARING" -> "Your order is being prepared — Halal Bite";
            case "READY"     -> "Your order is on its way! — Halal Bite";
            case "DELIVERED" -> "Order Delivered — Enjoy! — Halal Bite";
            case "CANCELLED" -> "Order Cancelled — Halal Bite #" + shortId(event.getOrderId());
            default          -> "Order Update — Halal Bite";
        };
    }

    private String getEmailForCustomer(UUID customerId, String emailFromEvent) {
        // Use email from event if provided, otherwise fallback
        if (emailFromEvent != null && !emailFromEvent.isBlank()) {
            return emailFromEvent;
        }
        // TODO: Call user-service via Feign to get customer email
        return "customer@halalbite.com";
    }

    private String shortId(UUID id) {
        // Show last 8 chars of UUID for readability in emails
        return id.toString().substring(id.toString().length() - 8).toUpperCase();
    }
}
