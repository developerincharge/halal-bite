package com.halalbite.menuservice.repository;

import com.halalbite.menuservice.entity.MenuCategory;
import com.halalbite.menuservice.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MenuCategoryRepository
 */
@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {

    // Get all active categories for a restaurant — ordered for display
    List<MenuCategory> findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(
        UUID restaurantId
    );

    // Get all categories (including inactive) — for restaurant owner management
    List<MenuCategory> findByRestaurantIdOrderByDisplayOrderAsc(UUID restaurantId);

    // Check category belongs to this restaurant (security check)
    Optional<MenuCategory> findByIdAndRestaurantId(UUID id, UUID restaurantId);

    boolean existsByRestaurantIdAndNameIgnoreCase(UUID restaurantId, String name);
}
