package com.halalbite.paymentservice.repository;

import com.halalbite.paymentservice.entity.Payment;
import com.halalbite.paymentservice.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    List<Payment> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<Payment> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId);

    List<Payment> findByStatus(PaymentStatus status);
}
