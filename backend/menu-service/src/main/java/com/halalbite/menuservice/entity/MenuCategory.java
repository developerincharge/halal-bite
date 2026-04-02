package com.halalbite.menuservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MenuCategory Entity — groups menu items into sections
 *
 * Examples: "Burgers", "Wraps", "Sides", "Drinks", "Desserts"
 *
 * Each category belongs to ONE restaurant (via restaurantId).
 * We store restaurantId as a plain UUID — not a @ManyToOne join —
 * because the Restaurant entity lives in a DIFFERENT service and database.
 * This is the correct microservices pattern: reference by ID, not by join.
 *
 * displayOrder controls how categories appear on the menu:
 * displayOrder=1 → shown first, displayOrder=2 → shown second etc.
 */
@Entity
@Table(name = "menu_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Reference to restaurant-service — stored as UUID, not a FK join
    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;          // e.g. "Burgers", "Drinks"

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // All items in this category
    @OneToMany(mappedBy = "category",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    @Builder.Default
    private List<MenuItem> items = new ArrayList<>();
}
