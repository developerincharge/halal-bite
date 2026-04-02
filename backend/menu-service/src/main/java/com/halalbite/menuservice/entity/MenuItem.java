package com.halalbite.menuservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MenuItem Entity — a single food item on the menu
 *
 * Key design decisions:
 *
 * 1. price uses BigDecimal — never double/float for money
 *    $12.99 stored exactly, no floating point rounding issues
 *
 * 2. discountedPrice — optional sale price
 *    When set, customers see the discounted price with original crossed out
 *    When null, full price applies
 *
 * 3. Dietary flags (isVegan, isGlutenFree, isSpicy)
 *    Boolean flags customers can filter by in the app
 *    All halal-bite items are halal by default (it's a halal platform)
 *
 * 4. isAvailable — restaurant can toggle this instantly
 *    e.g. "We've run out of chicken today" → set isAvailable = false
 *    Item stays on menu but shows as "Currently unavailable"
 *
 * 5. preparationTimeMinutes
 *    Feeds into the estimated delivery time shown to customers
 */
@Entity
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Foreign key to menu_categories table (same database — normal FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private MenuCategory category;

    // Denormalized for easy querying — same value as category.restaurantId
    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;              // e.g. "Big Halal Burger"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;       // e.g. "Juicy beef patty with..."

    @Column(name = "image_url")
    private String imageUrl;          // Link to food photo

    // Full price — always set
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Sale/discount price — null means no discount active
    @Column(name = "discounted_price", precision = 10, scale = 2)
    private BigDecimal discountedPrice;

    // Dietary information flags
    @Column(name = "is_vegan")
    @Builder.Default
    private Boolean isVegan = false;

    @Column(name = "is_vegetarian")
    @Builder.Default
    private Boolean isVegetarian = false;

    @Column(name = "is_gluten_free")
    @Builder.Default
    private Boolean isGlutenFree = false;

    @Column(name = "is_spicy")
    @Builder.Default
    private Boolean isSpicy = false;

    // Calories — shown on the menu for health-conscious customers
    @Column(name = "calories")
    private Integer calories;

    // How long to prepare — affects estimated delivery time
    @Column(name = "preparation_time_minutes")
    @Builder.Default
    private Integer preparationTimeMinutes = 15;

    // Restaurant can toggle this without deleting the item
    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    // Display order within the category
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
