package com.halalbite.menuservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Menu DTOs
 *
 * Category DTOs:
 *   CreateCategoryRequest  → create a new menu section
 *   UpdateCategoryRequest  → update category name/order
 *   CategoryResponse       → full category with all items
 *   CategorySummaryResponse → category name only (for nav)
 *
 * Item DTOs:
 *   CreateItemRequest      → add a food item to a category
 *   UpdateItemRequest      → update item details
 *   ItemResponse           → full item details
 */
public class MenuDto {

    // =====================================================
    // CATEGORY DTOs
    // =====================================================

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateCategoryRequest {

        @NotBlank(message = "Category name is required")
        @Size(max = 100)
        private String name;

        @Size(max = 500)
        private String description;

        private Integer displayOrder;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateCategoryRequest {
        private String name;
        private String description;
        private Integer displayOrder;
        private Boolean isActive;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CategoryResponse {
        private UUID id;
        private UUID restaurantId;
        private String name;
        private String description;
        private Integer displayOrder;
        private Boolean isActive;
        private List<ItemResponse> items;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CategorySummaryResponse {
        private UUID id;
        private String name;
        private Integer displayOrder;
        private Boolean isActive;
        private Integer itemCount;
    }

    // =====================================================
    // ITEM DTOs
    // =====================================================

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateItemRequest {

        @NotNull(message = "Category ID is required")
        private UUID categoryId;

        @NotBlank(message = "Item name is required")
        @Size(max = 200)
        private String name;

        @Size(max = 2000)
        private String description;

        private String imageUrl;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal price;

        private BigDecimal discountedPrice;
        private Boolean isVegan;
        private Boolean isVegetarian;
        private Boolean isGlutenFree;
        private Boolean isSpicy;
        private Integer calories;
        private Integer preparationTimeMinutes;
        private Integer displayOrder;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateItemRequest {
        private String name;
        private String description;
        private String imageUrl;
        private BigDecimal price;
        private BigDecimal discountedPrice;
        private Boolean isVegan;
        private Boolean isVegetarian;
        private Boolean isGlutenFree;
        private Boolean isSpicy;
        private Integer calories;
        private Integer preparationTimeMinutes;
        private Boolean isAvailable;
        private Integer displayOrder;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ItemResponse {
        private UUID id;
        private UUID categoryId;
        private UUID restaurantId;
        private String name;
        private String description;
        private String imageUrl;
        private BigDecimal price;
        private BigDecimal discountedPrice;
        private Boolean isVegan;
        private Boolean isVegetarian;
        private Boolean isGlutenFree;
        private Boolean isSpicy;
        private Integer calories;
        private Integer preparationTimeMinutes;
        private Boolean isAvailable;
        private Integer displayOrder;
        private LocalDateTime createdAt;
    }

    // Lightweight response for order-service price lookups
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ItemPriceResponse {
        private UUID id;
        private String name;
        private BigDecimal price;
        private BigDecimal discountedPrice;
        private Boolean isAvailable;
        private UUID restaurantId;
    }
}
