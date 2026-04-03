package com.halalbite.paymentservice.service;

import com.halalbite.paymentservice.dto.PaymentDto;
import com.halalbite.paymentservice.entity.Payment;
import com.halalbite.paymentservice.entity.PaymentStatus;
import com.halalbite.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;
    @InjectMocks private PaymentService paymentService;

    private final UUID ORDER_ID = UUID.randomUUID();
    private final UUID CUSTOMER_ID = UUID.randomUUID();
    private final UUID RESTAURANT_ID = UUID.randomUUID();

    private Payment testPayment;
    private PaymentDto.OrderPlacedEvent orderEvent;

    @BeforeEach
    void setUp() {
        testPayment = Payment.builder()
            .id(UUID.randomUUID())
            .orderId(ORDER_ID)
            .customerId(CUSTOMER_ID)
            .restaurantId(RESTAURANT_ID)
            .totalAmount(new BigDecimal("15.98"))
            .platformFeeAmount(new BigDecimal("1.95"))
            .restaurantAmount(new BigDecimal("14.03"))
            .status(PaymentStatus.PENDING)
            .stripePaymentIntentId("sim_test_123")
            .stripeClientSecret("sim_secret_456")
            .build();

        orderEvent = PaymentDto.OrderPlacedEvent.builder()
            .orderId(ORDER_ID)
            .customerId(CUSTOMER_ID)
            .restaurantId(RESTAURANT_ID)
            .totalAmount(new BigDecimal("15.98"))
            .platformFeeAmount(new BigDecimal("1.95"))
            .build();
    }

    @Test
    @DisplayName("createPayment — should create payment record from order event")
    void createPayment_success() {
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenReturn(testPayment);

        PaymentDto.PaymentResponse result = paymentService.createPayment(orderEvent);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(ORDER_ID);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("15.98");
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("createPayment — should return existing if already created (idempotency)")
    void createPayment_idempotent() {
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));

        paymentService.createPayment(orderEvent);

        // Should NOT save again — idempotent
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("simulatePaymentSuccess — should update status to SUCCEEDED and publish event")
    void simulatePaymentSuccess_success() {
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any())).thenReturn(testPayment);

        paymentService.simulatePaymentSuccess(ORDER_ID);

        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(testPayment.getPaidAt()).isNotNull();
        verify(kafkaTemplate).send(eq("payment.succeeded"), anyString(), any());
    }

    @Test
    @DisplayName("simulatePaymentFailure — should update status to FAILED and publish event")
    void simulatePaymentFailure_success() {
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any())).thenReturn(testPayment);

        paymentService.simulatePaymentFailure(ORDER_ID, "Card declined");

        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(testPayment.getFailureReason()).isEqualTo("Card declined");
        verify(kafkaTemplate).send(eq("payment.failed"), anyString(), any());
    }

    @Test
    @DisplayName("simulatePaymentSuccess — should not re-process already succeeded payment")
    void simulatePaymentSuccess_alreadySucceeded() {
        testPayment.setStatus(PaymentStatus.SUCCEEDED);
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));

        paymentService.simulatePaymentSuccess(ORDER_ID);

        // Already succeeded — should not save or publish again
        verify(paymentRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }
}
