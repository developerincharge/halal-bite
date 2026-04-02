package com.halalbite.restaurantservice.entity;

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
 * Restaurant Entity
 *
 * Key design decisions:
 *
 * 1. ownerKeycloakId
 *    Links the restaurant to the owner's Keycloak account.
 *    When a RESTAURANT_OWNER logs in, we find their restaurant
 *    using this field — same pattern as user-service.
 *
 * 2. affiliateRevenuePercentage (BigDecimal)
 *    The core of the halal-bite business model.
 *    This is the % the restaurant pays halal-bite per order.
 *    e.g. 0.15 = 15% of every order goes to halal-bite.
 *    Using BigDecimal (not double) for financial calculations —
 *    NEVER use float or double for money. Floating point
 *    arithmetic has rounding errors that cause financial bugs.
 *
 * 3. RestaurantStatus enum
 *    PENDING   — just registered, awaiting admin approval
 *    ACTIVE    — approved and visible to customers
 *    SUSPENDED — temporarily hidden (e.g. failed payment)
 *    CLOSED    — permanently closed, soft deleted
 *
 * 4. @OneToMany operatingHours
 *    Each restaurant has a separate operating hours record
 *    per day of the week (Monday → Sunday).
 */
@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Keycloak subject ID of the restaurant owner
    @Column(name = "owner_keycloak_id", nullable = false)
    private String ownerKeycloakId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cuisine_type", length = 100)
    private String cuisineType;    // e.g. "Pakistani", "Lebanese", "Turkish"

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "logo_url")
    private String logoUrl;

    // Full address fields
    @Column(name = "street_address")
    private String streetAddress;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country", length = 100)
    @Builder.Default
    private String country = "United States";

    // Halal-Bite affiliate model — % of each order paid to the platform
    // Default 15% — negotiated per restaurant
    @Column(name = "affiliate_revenue_percentage",
            nullable = false,
            precision = 5,
            scale = 2)
    @Builder.Default
    private BigDecimal affiliateRevenuePercentage = new BigDecimal("0.15");

    // Minimum order value in USD
    @Column(name = "minimum_order_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minimumOrderAmount = new BigDecimal("10.00");

    // Average delivery time in minutes
    @Column(name = "estimated_delivery_minutes")
    @Builder.Default
    private Integer estimatedDeliveryMinutes = 45;

    // Average rating (updated when reviews come in)
    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    // Halal certification details
    @Column(name = "is_halal_certified")
    @Builder.Default
    private Boolean isHalalCertified = false;

    @Column(name = "halal_certification_number", length = 100)
    private String halalCertificationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RestaurantStatus status = RestaurantStatus.PENDING;

    // Operating hours — one record per day of the week
    @OneToMany(mappedBy = "restaurant",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    @Builder.Default
    private List<OperatingHours> operatingHours = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
