package com.halalbite.menuservice.mapper;

import com.halalbite.menuservice.dto.MenuDto;
import com.halalbite.menuservice.entity.MenuCategory;
import com.halalbite.menuservice.entity.MenuItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MenuMapper {

    // Category mappings
    MenuDto.CategoryResponse toCategoryResponse(MenuCategory category);

    @Mapping(target = "itemCount", expression = "java(category.getItems().size())")
    MenuDto.CategorySummaryResponse toCategorySummaryResponse(MenuCategory category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurantId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "items", ignore = true)
    MenuCategory toCategoryEntity(MenuDto.CreateCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurantId", ignore = true)
    @Mapping(target = "items", ignore = true)
    void updateCategoryFromRequest(MenuDto.UpdateCategoryRequest request,
                                   @MappingTarget MenuCategory category);

    // Item mappings
    @Mapping(target = "categoryId", source = "category.id")
    MenuDto.ItemResponse toItemResponse(MenuItem item);

    // ItemPriceResponse has no categoryId field — map only what exists
    MenuDto.ItemPriceResponse toItemPriceResponse(MenuItem item);

    List<MenuDto.ItemResponse> toItemResponseList(List<MenuItem> items);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "restaurantId", ignore = true)
    @Mapping(target = "isAvailable", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MenuItem toItemEntity(MenuDto.CreateItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "restaurantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateItemFromRequest(MenuDto.UpdateItemRequest request,
                               @MappingTarget MenuItem item);
}
