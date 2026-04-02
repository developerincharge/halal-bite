package com.halalbite.menuservice.service;

import com.halalbite.menuservice.dto.MenuDto;
import com.halalbite.menuservice.entity.MenuCategory;
import com.halalbite.menuservice.entity.MenuItem;
import com.halalbite.menuservice.exception.MenuExceptions;
import com.halalbite.menuservice.mapper.MenuMapper;
import com.halalbite.menuservice.repository.MenuCategoryRepository;
import com.halalbite.menuservice.repository.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenuService Unit Tests")
class MenuServiceTest {

    @Mock private MenuCategoryRepository categoryRepository;
    @Mock private MenuItemRepository itemRepository;
    @Mock private MenuMapper menuMapper;
    @InjectMocks private MenuService menuService;

    private final UUID RESTAURANT_ID = UUID.randomUUID();
    private final UUID CATEGORY_ID = UUID.randomUUID();
    private final UUID ITEM_ID = UUID.randomUUID();

    private MenuCategory testCategory;
    private MenuItem testItem;

    @BeforeEach
    void setUp() {
        testCategory = MenuCategory.builder()
            .id(CATEGORY_ID)
            .restaurantId(RESTAURANT_ID)
            .name("Burgers")
            .isActive(true)
            .build();

        testItem = MenuItem.builder()
            .id(ITEM_ID)
            .category(testCategory)
            .restaurantId(RESTAURANT_ID)
            .name("Big Halal Burger")
            .price(new BigDecimal("12.99"))
            .isAvailable(true)
            .build();
    }

    @Test
    @DisplayName("createCategory — should create and return category")
    void createCategory_success() {
        MenuDto.CreateCategoryRequest request = MenuDto.CreateCategoryRequest.builder()
            .name("Burgers").build();
        MenuDto.CategoryResponse response = MenuDto.CategoryResponse.builder()
            .id(CATEGORY_ID).name("Burgers").build();

        when(categoryRepository.existsByRestaurantIdAndNameIgnoreCase(RESTAURANT_ID, "Burgers"))
            .thenReturn(false);
        when(menuMapper.toCategoryEntity(request)).thenReturn(testCategory);
        when(categoryRepository.save(any())).thenReturn(testCategory);
        when(menuMapper.toCategoryResponse(testCategory)).thenReturn(response);

        MenuDto.CategoryResponse result = menuService.createCategory(RESTAURANT_ID, request);

        assertThat(result.getName()).isEqualTo("Burgers");
        verify(categoryRepository).save(any(MenuCategory.class));
    }

    @Test
    @DisplayName("createCategory — should throw if duplicate name")
    void createCategory_duplicate_throws() {
        when(categoryRepository.existsByRestaurantIdAndNameIgnoreCase(RESTAURANT_ID, "Burgers"))
            .thenReturn(true);

        assertThatThrownBy(() -> menuService.createCategory(RESTAURANT_ID,
            MenuDto.CreateCategoryRequest.builder().name("Burgers").build()))
            .isInstanceOf(MenuExceptions.DuplicateCategoryException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("toggleItemAvailability — should flip availability flag")
    void toggleItemAvailability_success() {
        MenuDto.ItemResponse response = MenuDto.ItemResponse.builder()
            .id(ITEM_ID).isAvailable(false).build();

        when(itemRepository.findByIdAndRestaurantId(ITEM_ID, RESTAURANT_ID))
            .thenReturn(Optional.of(testItem));
        when(itemRepository.save(any())).thenReturn(testItem);
        when(menuMapper.toItemResponse(testItem)).thenReturn(response);

        menuService.toggleItemAvailability(RESTAURANT_ID, ITEM_ID);

        // isAvailable was true → should now be false
        assertThat(testItem.getIsAvailable()).isFalse();
    }

    @Test
    @DisplayName("getItemById — should throw if item not found")
    void getItemById_notFound_throws() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.getItemById(ITEM_ID))
            .isInstanceOf(MenuExceptions.ItemNotFoundException.class);
    }

    @Test
    @DisplayName("createItem — should throw if category does not belong to restaurant")
    void createItem_wrongCategory_throws() {
        when(categoryRepository.findByIdAndRestaurantId(CATEGORY_ID, RESTAURANT_ID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.createItem(RESTAURANT_ID,
            MenuDto.CreateItemRequest.builder()
                .categoryId(CATEGORY_ID)
                .name("Burger")
                .price(new BigDecimal("10.00"))
                .build()))
            .isInstanceOf(MenuExceptions.CategoryNotFoundException.class);
    }
}
