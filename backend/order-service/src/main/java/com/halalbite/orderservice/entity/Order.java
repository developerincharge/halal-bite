package com.halalbite.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Order Entity — the core aggregate root
 *
 * Key design decisions:
 *
 * 1. Prices are SNAPSHOTTED at order time
 *    itemPrice in OrderLineItem stores the price AT THE TIME of ordering.
 *    If the restaurant changes their menu price later, past orders
 *    are unaffected. This is critical for financial accuracy.
 *
 * 2. References by UUID — no FK joins to other services
 *    customerId → user-service
 *    restaurantId → restaurant-service
 *    These are stored as plain UUIDs, not JPA relationships.
 *
 * 3. platformFeeAmount — the halal-bite revenue
 *    Calculated as: subtotal × affiliateRevenuePercentage
 *    Stored on the order so payment-service knows exactly how
 *    much to transfer to the platform vs the restaurant.
 *
 * 4. deliveryAddress is denormalised here
 *    We copy the address at order time. If the user later
 *    changes their address, this order still has the correct
 *    delivery location.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // References to other services — stored as UUID, not FK
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    // All items in this order
    @OneToMany(mappedBy = "order",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    @Builder.Default
    private List<OrderLineItem> lineItems = new ArrayList<>();

    // Financial breakdown
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;           // sum of all item prices

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "platform_fee_amount", precision = 10, scale = 2)
    private BigDecimal platformFeeAmount;  // halal-bite's cut

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;        // subtotal + deliveryFee

    // Delivery address — snapshot at order time
    @Column(name = "delivery_street_address")
    private String deliveryStreetAddress;

    @Column(name = "delivery_city")
    private String deliveryCity;

    @Column(name = "delivery_state")
    private String deliveryState;

    @Column(name = "delivery_postal_code")
    private String deliveryPostalCode;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    // Set when payment-service confirms payment
    @Column(name = "payment_intent_id")
    private String paymentIntentId;

    // Estimated time restaurant will have order ready
    @Column(name = "estimated_ready_at")
    private LocalDateTime estimatedReadyAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
