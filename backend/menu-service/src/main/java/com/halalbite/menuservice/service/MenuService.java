package com.halalbite.menuservice.service;

import com.halalbite.menuservice.dto.MenuDto;
import com.halalbite.menuservice.entity.MenuCategory;
import com.halalbite.menuservice.entity.MenuItem;
import com.halalbite.menuservice.exception.MenuExceptions;
import com.halalbite.menuservice.mapper.MenuMapper;
import com.halalbite.menuservice.repository.MenuCategoryRepository;
import com.halalbite.menuservice.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Menu Service — business logic for menu management
 *
 * Key design decisions:
 *
 * 1. restaurantId ownership check
 *    Every write operation verifies the JWT userId matches the
 *    restaurant owner. We do this by checking the category's
 *    restaurantId matches what the restaurant-service tells us.
 *    For now we trust the restaurantId in the request — in a
 *    production app you'd call restaurant-service to verify ownership.
 *    TODO: Add service-to-service call to verify restaurant ownership
 *
 * 2. Separate category and item management
 *    RESTAURANT_OWNER creates categories first, then adds items to them.
 *    Customers see items grouped by category.
 *
 * 3. Availability toggle
 *    toggleItemAvailability() lets owners quickly mark items as
 *    unavailable without deleting them. This is a common operation
 *    (e.g. "we've run out of chicken wings today").
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;
    private final MenuMapper menuMapper;

    // =====================================================
    // CATEGORY OPERATIONS
    // =====================================================

    @Transactional
    public MenuDto.CategoryResponse createCategory(
            UUID restaurantId,
            MenuDto.CreateCategoryRequest request) {

        log.info("Creating category '{}' for restaurant: {}", request.getName(), restaurantId);

        if (categoryRepository.existsByRestaurantIdAndNameIgnoreCase(
                restaurantId, request.getName())) {
            throw new MenuExceptions.DuplicateCategoryException(
                "Category '" + request.getName() + "' already exists for this restaurant"
            );
        }

        MenuCategory category = menuMapper.toCategoryEntity(request);
        category.setRestaurantId(restaurantId);

        MenuCategory saved = categoryRepository.save(category);
        log.info("Category created: {}", saved.getId());
        return menuMapper.toCategoryResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MenuDto.CategoryResponse> getCategoriesWithItems(UUID restaurantId) {
        log.debug("Fetching full menu for restaurant: {}", restaurantId);
        return categoryRepository
            .findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(restaurantId)
            .stream()
            .map(menuMapper::toCategoryResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuDto.CategorySummaryResponse> getCategorySummaries(UUID restaurantId) {
        return categoryRepository
            .findByRestaurantIdOrderByDisplayOrderAsc(restaurantId)
            .stream()
            .map(menuMapper::toCategorySummaryResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public MenuDto.CategoryResponse updateCategory(
            UUID restaurantId,
            UUID categoryId,
            MenuDto.UpdateCategoryRequest request) {

        MenuCategory category = findCategoryByIdAndRestaurant(categoryId, restaurantId);
        menuMapper.updateCategoryFromRequest(request, category);
        return menuMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(UUID restaurantId, UUID categoryId) {
        MenuCategory category = findCategoryByIdAndRestaurant(categoryId, restaurantId);
        log.info("Deleting category: {} for restaurant: {}", categoryId, restaurantId);
        categoryRepository.delete(category);
    }

    // =====================================================
    // ITEM OPERATIONS
    // =====================================================

    @Transactional
    public MenuDto.ItemResponse createItem(
            UUID restaurantId,
            MenuDto.CreateItemRequest request) {

        log.info("Creating item '{}' in category: {}", request.getName(), request.getCategoryId());

        // Verify the category belongs to this restaurant
        MenuCategory category = findCategoryByIdAndRestaurant(
            request.getCategoryId(), restaurantId
        );

        MenuItem item = menuMapper.toItemEntity(request);
        item.setCategory(category);
        item.setRestaurantId(restaurantId);

        MenuItem saved = itemRepository.save(item);
        log.info("Menu item created: {}", saved.getId());
        return menuMapper.toItemResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MenuDto.ItemResponse> getItemsByCategory(UUID categoryId) {
        return menuMapper.toItemResponseList(
            itemRepository.findByCategoryIdAndIsAvailableTrueOrderByDisplayOrderAsc(categoryId)
        );
    }

    @Transactional(readOnly = true)
    public List<MenuDto.ItemResponse> getAllItemsByRestaurant(UUID restaurantId) {
        return menuMapper.toItemResponseList(
            itemRepository.findByRestaurantIdAndIsAvailableTrueOrderByDisplayOrderAsc(restaurantId)
        );
    }

    @Transactional(readOnly = true)
    public MenuDto.ItemResponse getItemById(UUID itemId) {
        return menuMapper.toItemResponse(findItemById(itemId));
    }

    // Used by order-service to get the current price of an item
    @Transactional(readOnly = true)
    public MenuDto.ItemPriceResponse getItemPrice(UUID itemId) {
        MenuItem item = findItemById(itemId);
        return menuMapper.toItemPriceResponse(item);
    }

    @Transactional(readOnly = true)
    public List<MenuDto.ItemResponse> searchItems(UUID restaurantId, String query) {
        return menuMapper.toItemResponseList(
            itemRepository.searchByName(restaurantId, query)
        );
    }

    @Transactional
    public MenuDto.ItemResponse updateItem(
            UUID restaurantId,
            UUID itemId,
            MenuDto.UpdateItemRequest request) {

        MenuItem item = findItemByIdAndRestaurant(itemId, restaurantId);
        menuMapper.updateItemFromRequest(request, item);
        return menuMapper.toItemResponse(itemRepository.save(item));
    }

    /**
     * Toggle item availability — most common restaurant operation.
     * Runs out of stock? Call this. Back in stock? Call this again.
     */
    @Transactional
    public MenuDto.ItemResponse toggleItemAvailability(UUID restaurantId, UUID itemId) {
        MenuItem item = findItemByIdAndRestaurant(itemId, restaurantId);
        item.setIsAvailable(!item.getIsAvailable());
        log.info("Item {} availability toggled to: {}", itemId, item.getIsAvailable());
        return menuMapper.toItemResponse(itemRepository.save(item));
    }

    @Transactional
    public void deleteItem(UUID restaurantId, UUID itemId) {
        MenuItem item = findItemByIdAndRestaurant(itemId, restaurantId);
        log.info("Deleting item: {} from restaurant: {}", itemId, restaurantId);
        itemRepository.delete(item);
    }

    // =====================================================
    // Private helpers
    // =====================================================

    private MenuCategory findCategoryByIdAndRestaurant(UUID categoryId, UUID restaurantId) {
        return categoryRepository
            .findByIdAndRestaurantId(categoryId, restaurantId)
            .orElseThrow(() -> new MenuExceptions.CategoryNotFoundException(
                "Category not found: " + categoryId
            ));
    }

    private MenuItem findItemById(UUID itemId) {
        return itemRepository.findById(itemId)
            .orElseThrow(() -> new MenuExceptions.ItemNotFoundException(
                "Menu item not found: " + itemId
            ));
    }

    private MenuItem findItemByIdAndRestaurant(UUID itemId, UUID restaurantId) {
        return itemRepository
            .findByIdAndRestaurantId(itemId, restaurantId)
            .orElseThrow(() -> new MenuExceptions.ItemNotFoundException(
                "Menu item not found or does not belong to this restaurant: " + itemId
            ));
    }
}
