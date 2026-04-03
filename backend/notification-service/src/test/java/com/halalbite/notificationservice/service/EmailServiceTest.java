package com.halalbite.notificationservice.service;

import com.halalbite.notificationservice.dto.NotificationDto;
import com.halalbite.notificationservice.entity.NotificationLog;
import com.halalbite.notificationservice.entity.NotificationStatus;
import com.halalbite.notificationservice.repository.NotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Unit Tests")
class EmailServiceTest {

    @Mock private JavaMailSender mailSender;
    @Mock private NotificationLogRepository notificationLogRepository;
    @InjectMocks private EmailService emailService;

    private final UUID ORDER_ID  = UUID.randomUUID();
    private final UUID CUSTOMER_ID = UUID.randomUUID();

    @Test
    @DisplayName("sendOrderConfirmation — should send email and log as SENT")
    void sendOrderConfirmation_success() {
        NotificationDto.OrderPlacedEvent event = NotificationDto.OrderPlacedEvent.builder()
            .orderId(ORDER_ID)
            .customerId(CUSTOMER_ID)
            .totalAmount(new BigDecimal("15.98"))
            .customerEmail("customer@halalbite.com")
            .createdAt(LocalDateTime.now())
            .build();

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        when(notificationLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        emailService.sendOrderConfirmation(event);

        verify(mailSender).send(any(SimpleMailMessage.class));

        ArgumentCaptor<NotificationLog> logCaptor =
            ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(logCaptor.capture());

        NotificationLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(savedLog.getNotificationType()).isEqualTo("ORDER_CONFIRMED");
        assertThat(savedLog.getRecipientEmail()).isEqualTo("customer@halalbite.com");
    }

    @Test
    @DisplayName("sendOrderConfirmation — should log as FAILED when mail sender throws")
    void sendOrderConfirmation_mailFails_logsFailure() {
        NotificationDto.OrderPlacedEvent event = NotificationDto.OrderPlacedEvent.builder()
            .orderId(ORDER_ID)
            .customerId(CUSTOMER_ID)
            .totalAmount(new BigDecimal("15.98"))
            .customerEmail("customer@halalbite.com")
            .createdAt(LocalDateTime.now())
            .build();

        doThrow(new RuntimeException("SMTP connection failed"))
            .when(mailSender).send(any(SimpleMailMessage.class));
        when(notificationLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Should NOT throw — service handles the error gracefully
        emailService.sendOrderConfirmation(event);

        ArgumentCaptor<NotificationLog> logCaptor =
            ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(logCaptor.capture());

        assertThat(logCaptor.getValue().getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(logCaptor.getValue().getErrorMessage()).contains("SMTP");
    }

    @Test
    @DisplayName("sendPaymentReceipt — should send and log correctly")
    void sendPaymentReceipt_success() {
        NotificationDto.PaymentSucceededEvent event = NotificationDto.PaymentSucceededEvent.builder()
            .paymentId(UUID.randomUUID())
            .orderId(ORDER_ID)
            .customerId(CUSTOMER_ID)
            .totalAmount(new BigDecimal("15.98"))
            .paidAt(LocalDateTime.now())
            .build();

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        when(notificationLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        emailService.sendPaymentReceipt(event);

        verify(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<NotificationLog> logCaptor =
            ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getNotificationType()).isEqualTo("PAYMENT_RECEIPT");
    }
}
