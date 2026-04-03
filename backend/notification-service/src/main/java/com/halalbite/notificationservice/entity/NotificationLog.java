package com.halalbite.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * NotificationLog — audit trail of every notification sent
 *
 * Why log notifications?
 * - Debug: "Did the customer receive the confirmation email?"
 * - Retry: If an email failed, we can resend it
 * - Audit: Compliance requirement to prove notifications were sent
 * - Analytics: Track open rates and delivery success rates
 */
@Entity
@Table(name = "notification_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "recipient_id")
    private UUID recipientId;          // customer or restaurant UUID

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;   // ORDER_CONFIRMED, PAYMENT_SUCCESS etc.

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "reference_id")
    private UUID referenceId;          // orderId or paymentId

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.SENT;

    @Column(name = "error_message")
    private String errorMessage;       // Set if status = FAILED

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
