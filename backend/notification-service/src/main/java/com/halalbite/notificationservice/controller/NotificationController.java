package com.halalbite.notificationservice.controller;

import com.halalbite.notificationservice.entity.NotificationLog;
import com.halalbite.notificationservice.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Notification Controller
 *
 * Read-only endpoints to inspect notification history.
 * Useful for debugging: "Did the customer get their confirmation email?"
 *
 * Endpoints:
 *   GET /api/v1/notifications/my              → my notification history
 *   GET /api/v1/notifications/order/{orderId} → all notifications for an order
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationLogRepository notificationLogRepository;

    /**
     * GET /api/v1/notifications/my
     * Get all notifications sent to the authenticated user
     */
    @GetMapping("/my")
    public ResponseEntity<List<NotificationLog>> getMyNotifications(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(
            notificationLogRepository.findByRecipientIdOrderByCreatedAtDesc(userId)
        );
    }

    /**
     * GET /api/v1/notifications/order/{orderId}
     * Get all notifications sent for a specific order
     * Useful for support: "What emails were sent for this order?"
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<NotificationLog>> getOrderNotifications(
            @PathVariable UUID orderId) {

        return ResponseEntity.ok(
            notificationLogRepository.findByReferenceIdOrderByCreatedAtDesc(orderId)
        );
    }
}
