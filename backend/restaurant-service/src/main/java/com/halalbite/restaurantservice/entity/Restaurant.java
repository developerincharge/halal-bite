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
 * ownerUserId — the UUID from auth-service (JWT subject claim).
 * When a RESTAURANT_OWNER logs in, their JWT subject is their userId
 * from auth-service. We store it here to find their restaurant.
 *
 * IMPORTANT: Run this SQL if you still have old column name:
 *   ALTER TABLE restaurants RENAME COLUMN owner_keycloak_id TO owner_user_id;
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

    // JWT subject (UUID) from auth-service — identifies the restaurant owner
    @Column(name = "owner_user_id", nullable = false)
    private String ownerUserId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cuisine_type", length = 100)
    private String cuisineType;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "logo_url")
    private String logoUrl;

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

    // 0.15 = 15% of every order goes to halal-bite platform
    @Column(name = "affiliate_revenue_percentage",
            nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal affiliateRevenuePercentage = new BigDecimal("0.15");

    @Column(name = "minimum_order_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minimumOrderAmount = new BigDecimal("10.00");

    @Column(name = "estimated_delivery_minutes")
    @Builder.Default
    private Integer estimatedDeliveryMinutes = 45;

    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "is_halal_certified")
    @Builder.Default
    private Boolean isHalalCertified = false;

    @Column(name = "halal_certification_number", length = 100)
    private String halalCertificationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RestaurantStatus status = RestaurantStatus.PENDING;

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