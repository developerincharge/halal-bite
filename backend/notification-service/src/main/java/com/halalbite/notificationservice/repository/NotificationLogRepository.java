package com.halalbite.notificationservice.repository;

import com.halalbite.notificationservice.entity.NotificationLog;
import com.halalbite.notificationservice.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    List<NotificationLog> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    List<NotificationLog> findByReferenceIdOrderByCreatedAtDesc(UUID referenceId);

    List<NotificationLog> findByStatus(NotificationStatus status);
}
