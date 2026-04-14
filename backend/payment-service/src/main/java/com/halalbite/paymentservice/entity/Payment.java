package com.halalbite.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.halalbite.paymentservice.entity.PaymentStatus;
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

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "restaurant_id")
    private UUID restaurantId;

    @Column(name = "paypal_payment_id")
    private String paypalPaymentId;

    @Column(name = "paypal_payer_id")
    private String paypalPayerId;

    @Column(name = "approval_url", length = 1000)
    private String approvalUrl;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "restaurant_amount", precision = 10, scale = 2)
    private BigDecimal restaurantAmount;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "platform_fee_amount", precision = 10, scale = 2)
    private BigDecimal platformFeeAmount;

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
