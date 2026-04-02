package com.halalbite.menuservice.repository;

import com.halalbite.menuservice.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    // All available items for a restaurant — for customer browsing
    List<MenuItem> findByRestaurantIdAndIsAvailableTrueOrderByDisplayOrderAsc(
        UUID restaurantId
    );

    // All items in a category — for customer browsing
    List<MenuItem> findByCategoryIdAndIsAvailableTrueOrderByDisplayOrderAsc(
        UUID categoryId
    );

    // All items in a category including unavailable — for owner management
    List<MenuItem> findByCategoryIdOrderByDisplayOrderAsc(UUID categoryId);

    // Search items by name within a restaurant
    @Query("""
        SELECT i FROM MenuItem i
        WHERE i.restaurantId = :restaurantId
        AND i.isAvailable = true
        AND LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY i.displayOrder ASC
        """)
    List<MenuItem> searchByName(
        @Param("restaurantId") UUID restaurantId,
        @Param("query") String query
    );

    // Filter by dietary flags
    List<MenuItem> findByRestaurantIdAndIsVeganTrueAndIsAvailableTrue(
        UUID restaurantId
    );
    List<MenuItem> findByRestaurantIdAndIsGlutenFreeTrueAndIsAvailableTrue(
        UUID restaurantId
    );

    // Security check — item belongs to this restaurant
    Optional<MenuItem> findByIdAndRestaurantId(UUID id, UUID restaurantId);
}
