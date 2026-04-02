package com.halalbite.menuservice.controller;

import com.halalbite.menuservice.dto.MenuDto;
import com.halalbite.menuservice.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Menu Controller
 *
 * URL structure:
 *   /api/v1/menus/restaurants/{restaurantId}/categories      → category management
 *   /api/v1/menus/restaurants/{restaurantId}/items           → item management
 *   /api/v1/menus/items/{itemId}                             → item lookup (for order-service)
 *
 * Public endpoints (no JWT needed):
 *   GET categories and items — customers browse menus without logging in
 *
 * Protected endpoints (JWT required):
 *   POST/PATCH/DELETE — only restaurant owners can modify their menu
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // =====================================================
    // CATEGORY ENDPOINTS
    // =====================================================

    /**
     * POST /api/v1/menus/restaurants/{restaurantId}/categories
     * Create a new menu category — RESTAURANT_OWNER only
     *
     * Postman body:
     * { "name": "Burgers", "description": "Our signature halal burgers", "displayOrder": 1 }
     */
    @PostMapping("/restaurants/{restaurantId}/categories")
    public ResponseEntity<MenuDto.CategoryResponse> createCategory(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody MenuDto.CreateCategoryRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        log.info("POST categories for restaurant: {} by user: {}",
            restaurantId, jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(menuService.createCategory(restaurantId, request));
    }

    /**
     * GET /api/v1/menus/restaurants/{restaurantId}/categories
     * Get full menu with all categories and items — PUBLIC
     *
     * This is what the customer app calls to display the restaurant menu.
     */
    @GetMapping("/restaurants/{restaurantId}/categories")
    public ResponseEntity<List<MenuDto.CategoryResponse>> getMenu(
            @PathVariable UUID restaurantId) {

        log.debug("GET full menu for restaurant: {}", restaurantId);
        return ResponseEntity.ok(menuService.getCategoriesWithItems(restaurantId));
    }

    /**
     * GET /api/v1/menus/restaurants/{restaurantId}/categories/summary
     * Get category names only — for navigation tabs in the app
     */
    @GetMapping("/restaurants/{restaurantId}/categories/summary")
    public ResponseEntity<List<MenuDto.CategorySummaryResponse>> getCategorySummaries(
            @PathVariable UUID restaurantId) {

        return ResponseEntity.ok(menuService.getCategorySummaries(restaurantId));
    }

    /**
     * PATCH /api/v1/menus/restaurants/{restaurantId}/categories/{categoryId}
     * Update a category — RESTAURANT_OWNER only
     */
    @PatchMapping("/restaurants/{restaurantId}/categories/{categoryId}")
    public ResponseEntity<MenuDto.CategoryResponse> updateCategory(
            @PathVariable UUID restaurantId,
            @PathVariable UUID categoryId,
            @RequestBody MenuDto.UpdateCategoryRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
            menuService.updateCategory(restaurantId, categoryId, request)
        );
    }

    /**
     * DELETE /api/v1/menus/restaurants/{restaurantId}/categories/{categoryId}
     * Delete a category and all its items — RESTAURANT_OWNER only
     */
    @DeleteMapping("/restaurants/{restaurantId}/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID restaurantId,
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal Jwt jwt) {

        menuService.deleteCategory(restaurantId, categoryId);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // ITEM ENDPOINTS
    // =====================================================

    /**
     * POST /api/v1/menus/restaurants/{restaurantId}/items
     * Add a food item to the menu — RESTAURANT_OWNER only
     *
     * Postman body:
     * {
     *   "categoryId": "uuid-of-burgers-category",
     *   "name": "Big Halal Burger",
     *   "description": "Juicy beef patty with fresh vegetables",
     *   "price": 12.99,
     *   "isSpicy": false,
     *   "calories": 650,
     *   "preparationTimeMinutes": 15,
     *   "displayOrder": 1
     * }
     */
    @PostMapping("/restaurants/{restaurantId}/items")
    public ResponseEntity<MenuDto.ItemResponse> createItem(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody MenuDto.CreateItemRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        log.info("POST item '{}' for restaurant: {} by user: {}",
            request.getName(), restaurantId, jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(menuService.createItem(restaurantId, request));
    }

    /**
     * GET /api/v1/menus/restaurants/{restaurantId}/items
     * Get all available items for a restaurant — PUBLIC
     */
    @GetMapping("/restaurants/{restaurantId}/items")
    public ResponseEntity<List<MenuDto.ItemResponse>> getAllItems(
            @PathVariable UUID restaurantId) {

        return ResponseEntity.ok(menuService.getAllItemsByRestaurant(restaurantId));
    }

    /**
     * GET /api/v1/menus/restaurants/{restaurantId}/items/search?query=burger
     * Search items by name — PUBLIC
     */
    @GetMapping("/restaurants/{restaurantId}/items/search")
    public ResponseEntity<List<MenuDto.ItemResponse>> searchItems(
            @PathVariable UUID restaurantId,
            @RequestParam String query) {

        return ResponseEntity.ok(menuService.searchItems(restaurantId, query));
    }

    /**
     * GET /api/v1/menus/items/{itemId}
     * Get a single item by ID — PUBLIC
     * Used by order-service to validate items when placing an order
     */
    @GetMapping("/items/{itemId}")
    public ResponseEntity<MenuDto.ItemResponse> getItemById(
            @PathVariable UUID itemId) {

        return ResponseEntity.ok(menuService.getItemById(itemId));
    }

    /**
     * GET /api/v1/menus/items/{itemId}/price
     * Get item price only — for order-service internal use
     */
    @GetMapping("/items/{itemId}/price")
    public ResponseEntity<MenuDto.ItemPriceResponse> getItemPrice(
            @PathVariable UUID itemId) {

        return ResponseEntity.ok(menuService.getItemPrice(itemId));
    }

    /**
     * PATCH /api/v1/menus/restaurants/{restaurantId}/items/{itemId}
     * Update a menu item — RESTAURANT_OWNER only
     */
    @PatchMapping("/restaurants/{restaurantId}/items/{itemId}")
    public ResponseEntity<MenuDto.ItemResponse> updateItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @RequestBody MenuDto.UpdateItemRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(menuService.updateItem(restaurantId, itemId, request));
    }

    /**
     * PATCH /api/v1/menus/restaurants/{restaurantId}/items/{itemId}/toggle-availability
     * Toggle item availability on/off — RESTAURANT_OWNER only
     * Most common operation: run out of stock? Call this.
     */
    @PatchMapping("/restaurants/{restaurantId}/items/{itemId}/toggle-availability")
    public ResponseEntity<MenuDto.ItemResponse> toggleAvailability(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @AuthenticationPrincipal Jwt jwt) {

        log.info("Toggling availability for item: {} restaurant: {}", itemId, restaurantId);
        return ResponseEntity.ok(
            menuService.toggleItemAvailability(restaurantId, itemId)
        );
    }

    /**
     * DELETE /api/v1/menus/restaurants/{restaurantId}/items/{itemId}
     * Delete a menu item — RESTAURANT_OWNER only
     */
    @DeleteMapping("/restaurants/{restaurantId}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @AuthenticationPrincipal Jwt jwt) {

        menuService.deleteItem(restaurantId, itemId);
        return ResponseEntity.noContent().build();
    }
}
