package com.halalbite.userservice.repository;

import com.halalbite.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * User Repository — database access layer for User entity
 *
 * Spring Data JPA generates the SQL automatically from method names.
 * You don't write SQL — you write descriptive method names and
 * Spring figures out the query.
 *
 * Examples of what Spring generates:
 *   findByEmail("john@example.com")
 *   → SELECT * FROM users WHERE email = 'john@example.com'
 *
 *   findByKeycloakId("abc-123")
 *   → SELECT * FROM users WHERE keycloak_id = 'abc-123'
 *
 *   existsByEmail("john@example.com")
 *   → SELECT COUNT(*) > 0 FROM users WHERE email = 'john@example.com'
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Find a user by their Keycloak subject ID (from JWT token)
    Optional<User> findByKeycloakId(String keycloakId);

    // Find a user by email address
    Optional<User> findByEmail(String email);

    // Check if an email is already registered (used during registration)
    boolean existsByEmail(String email);

    // Find only active users by Keycloak ID
    Optional<User> findByKeycloakIdAndIsActiveTrue(String keycloakId);
}
