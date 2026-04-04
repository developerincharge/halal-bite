package com.halalbite.restaurantservice.mapper;

import com.halalbite.restaurantservice.dto.RestaurantDto;
import com.halalbite.restaurantservice.entity.OperatingHours;
import com.halalbite.restaurantservice.entity.Restaurant;
import org.mapstruct.*;

/**
 * RestaurantMapper — MapStruct mapper for Restaurant entity ↔ DTOs
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RestaurantMapper {

    RestaurantDto.RestaurantResponse toResponse(Restaurant restaurant);

    RestaurantDto.RestaurantSummaryResponse toSummaryResponse(Restaurant restaurant);

    RestaurantDto.OperatingHoursResponse toOperatingHoursResponse(OperatingHours hours);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerUserId", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "operatingHours", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Restaurant toEntity(RestaurantDto.CreateRestaurantRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerUserId", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "operatingHours", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(RestaurantDto.UpdateRestaurantRequest request,
                                  @MappingTarget Restaurant restaurant);
}
