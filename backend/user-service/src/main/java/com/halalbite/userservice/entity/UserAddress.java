package com.halalbite.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * UserAddress Entity — maps to "user_addresses" table
 *
 * A user can save multiple delivery addresses.
 * One is marked as default (isDefault = true).
 * When placing an order, the app shows all saved addresses.
 *
 * Relationship: Many addresses belong to one User (@ManyToOne)
 */
@Entity
@Table(name = "user_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Foreign key back to the users table
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "label", length = 50)
    private String label;         // e.g. "Home", "Work", "Mum's house"

    @Column(name = "street_address", nullable = false)
    private String streetAddress; // e.g. "123 Main Street, Apt 4B"

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "country", nullable = false, length = 100)
    @Builder.Default
    private String country = "United States";

    // Only one address per user can be the default
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}
