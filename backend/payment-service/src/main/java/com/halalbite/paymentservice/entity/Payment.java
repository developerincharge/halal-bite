package com.halalbite.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment Entity — one record per order payment
 *
 * Key fields:
 *
 * stripePaymentIntentId
 *   The Stripe reference for this payment. Used to confirm,
 *   cancel, or refund the payment through Stripe's API.
 *
 * totalAmount vs platformFeeAmount vs restaurantAmount
 *   totalAmount      = what the customer pays ($15.98)
 *   platformFeeAmount = halal-bite's cut ($2.40 + $2.99 delivery)
 *   restaurantAmount  = what the restaurant receives ($10.59)
 *   These three must always balance: total = platform + restaurant
 *
 * stripeTransferGroup
 *   Groups related Stripe transfers so you can audit them.
 *   Format: "order-{orderId}"
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "order_id", unique = true, nullable = false)
    private UUID orderId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    // Stripe references
    @Column(name = "stripe_payment_intent_id", unique = true)
    private String stripePaymentIntentId;

    @Column(name = "stripe_client_secret")
    private String stripeClientSecret;  // Sent to frontend to complete payment

    @Column(name = "stripe_transfer_group")
    private String stripeTransferGroup;

    // Financial amounts — all in USD
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "platform_fee_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal platformFeeAmount;

    @Column(name = "restaurant_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal restaurantAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "failure_reason")
    private String failureReason;

    // Set when payment is confirmed
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
