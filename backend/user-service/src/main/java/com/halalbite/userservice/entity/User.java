package com.halalbite.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User Entity — maps directly to the "users" table in user_service_db
 *
 * Key design decisions:
 *
 * 1. UUID as primary key (not Long/Integer)
 *    Why? UUIDs are globally unique across all services and databases.
 *    If you ever merge databases or share IDs between services,
 *    there is zero collision risk. Long IDs like 1, 2, 3 would conflict.
 *
 * 2. keycloakId field
 *    Keycloak manages authentication (login, passwords, tokens).
 *    This service manages the business profile (name, address, preferences).
 *    The keycloakId links the two — when a JWT token arrives,
 *    we extract the Keycloak subject ID and look up the user profile here.
 *
 * 3. @OneToMany addresses
 *    A user can have multiple delivery addresses (home, work, etc.)
 *    Stored in a separate "user_addresses" table.
 *    CascadeType.ALL means when a user is deleted, their addresses are too.
 *
 * 4. Soft delete (isActive flag)
 *    We never hard-delete users. Setting isActive=false "deletes" them.
 *    This preserves order history and audit trails.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Links this profile to the Keycloak account
    // Extracted from the JWT token's "sub" claim
    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // One user can have many saved delivery addresses
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserAddress> addresses = new ArrayList<>();

    // Soft delete — never remove a user, just deactivate them
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Automatically set by Hibernate on INSERT
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Automatically updated by Hibernate on every UPDATE
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
