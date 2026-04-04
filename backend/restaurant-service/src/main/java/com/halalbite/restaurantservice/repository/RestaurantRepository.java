package com.halalbite.restaurantservice.repository;

import com.halalbite.restaurantservice.entity.Restaurant;
import com.halalbite.restaurantservice.entity.RestaurantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * RestaurantRepository
 *
 * IMPORTANT: Every method name after findBy must match the exact
 * Java field name in Restaurant.java — NOT the database column name.
 *
 * Our field is: ownerUserId
 * So the method is: findByOwnerUserId  ✅
 * NOT:             findByOwnerId       ❌
 * NOT:             findByOwnerKeycloakId ❌
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    // Find restaurants by owner's userId (JWT subject from auth-service)
    List<Restaurant> findByOwnerUserId(String ownerUserId);

    // Find single restaurant by owner — used when owner manages their restaurant
    Optional<Restaurant> findFirstByOwnerUserId(String ownerUserId);

    // Check if owner already has a restaurant registered
    boolean existsByOwnerUserId(String ownerUserId);

    // List active restaurants — paginated for customer browsing
    Page<Restaurant> findByStatus(RestaurantStatus status, Pageable pageable);

    // List restaurants by status (without pagination) — used by admin
    List<Restaurant> findByStatus(RestaurantStatus status);

    // Search active restaurants by name or cuisine type
    @Query("""
        SELECT r FROM Restaurant r
        WHERE r.status = 'ACTIVE'
        AND (LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(r.cuisineType) LIKE LOWER(CONCAT('%', :query, '%')))
        """)
    Page<Restaurant> searchActiveRestaurants(
        @Param("query") String query,
        Pageable pageable
    );

    // Find active restaurants by city
    Page<Restaurant> findByStatusAndCityIgnoreCase(
        RestaurantStatus status,
        String city,
        Pageable pageable
    );
}
