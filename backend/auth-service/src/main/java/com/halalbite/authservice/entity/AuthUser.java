package com.halalbite.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AuthUser Entity — stored in auth_service_db
 *
 * This entity only stores authentication data:
 *   - email (login identifier)
 *   - password (BCrypt hashed — NEVER store plain text)
 *   - role (CUSTOMER, RESTAURANT_OWNER, ADMIN)
 *
 * It does NOT store profile data (name, address etc.)
 * Profile data lives in user-service's user_service_db.
 *
 * Why separate auth data from profile data?
 * - Auth service only cares about "is this person who they say they are?"
 * - User service cares about "what do we know about this person?"
 * - Keeping them separate means you could swap auth systems later
 *   without touching your user profiles.
 *
 * The UUID id here becomes the "sub" claim in the JWT token.
 * Other services use this UUID to identify the user.
 */
@Entity
@Table(name = "auth_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    // BCrypt hashed — Spring's PasswordEncoder handles this automatically
    // BCrypt hash looks like: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    @Builder.Default
    private UserRole role = UserRole.CUSTOMER;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
