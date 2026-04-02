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
 * Restaurant Repository
 *
 * Note the use of Page<Restaurant> for listing restaurants.
 * Customers browse restaurants — there could be hundreds.
 * We never return ALL restaurants in one response — always paginated.
 * Pageable lets the caller specify page number, page size, and sort order.
 *
 * Example usage in service:
 *   Pageable pageable = PageRequest.of(0, 20, Sort.by("name"));
 *   Page<Restaurant> page = repository.findByStatus(ACTIVE, pageable);
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    // Find a restaurant by its owner's Keycloak ID
    // Used when RESTAURANT_OWNER logs in to manage their restaurant
    Optional<Restaurant> findByOwnerKeycloakId(String ownerKeycloakId);

    // List all active restaurants — paginated (for customer browsing)
    Page<Restaurant> findByStatus(RestaurantStatus status, Pageable pageable);

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

    // Find restaurants by cuisine type
    Page<Restaurant> findByStatusAndCuisineTypeIgnoreCase(
        RestaurantStatus status,
        String cuisineType,
        Pageable pageable
    );

    // Check if owner already has a restaurant registered
    boolean existsByOwnerKeycloakId(String ownerKeycloakId);

    // List restaurants by status — used by admin
    List<Restaurant> findByStatus(RestaurantStatus status);
}
