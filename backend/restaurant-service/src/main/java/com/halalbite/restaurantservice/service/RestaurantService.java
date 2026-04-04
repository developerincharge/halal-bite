package com.halalbite.restaurantservice.service;

import com.halalbite.restaurantservice.dto.RestaurantDto;
import com.halalbite.restaurantservice.entity.OperatingHours;
import com.halalbite.restaurantservice.entity.Restaurant;
import com.halalbite.restaurantservice.entity.RestaurantStatus;
import com.halalbite.restaurantservice.exception.RestaurantExceptions;
import com.halalbite.restaurantservice.mapper.RestaurantMapper;
import com.halalbite.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Restaurant Service — business logic layer
 *
 * ownerUserId = the JWT subject from auth-service.
 * Every method that modifies a restaurant verifies the caller
 * owns that restaurant before allowing the change.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;

    /**
     * Register a new restaurant.
     * Called by RESTAURANT_OWNER after signing up.
     * Status starts as PENDING — admin must approve before customers see it.
     */
    @Transactional
    public RestaurantDto.RestaurantResponse createRestaurant(
            RestaurantDto.CreateRestaurantRequest request,
            String ownerUserId) {

        log.info("Creating restaurant for owner: {}", ownerUserId);

        if (restaurantRepository.existsByOwnerUserId(ownerUserId)) {
            throw new RestaurantExceptions.RestaurantAlreadyExistsException(
                "You already have a restaurant registered. Contact support to register another."
            );
        }

        Restaurant restaurant = restaurantMapper.toEntity(request);
        restaurant.setOwnerUserId(ownerUserId);
        restaurant.setStatus(RestaurantStatus.PENDING);

        // Set defaults manually — MapStruct bypasses @Builder.Default
        if (restaurant.getAffiliateRevenuePercentage() == null) {
            restaurant.setAffiliateRevenuePercentage(new BigDecimal("0.15"));
        }
        if (restaurant.getMinimumOrderAmount() == null) {
            restaurant.setMinimumOrderAmount(new BigDecimal("10.00"));
        }
        if (restaurant.getEstimatedDeliveryMinutes() == null) {
            restaurant.setEstimatedDeliveryMinutes(45);
        }
        if (restaurant.getAverageRating() == null) {
            restaurant.setAverageRating(BigDecimal.ZERO);
        }
        if (restaurant.getTotalReviews() == null) {
            restaurant.setTotalReviews(0);
        }

        // Add operating hours if provided in request
        if (request.getOperatingHours() != null) {
            List<OperatingHours> hours = request.getOperatingHours().stream()
                .map(h -> OperatingHours.builder()
                    .restaurant(restaurant)
                    .dayOfWeek(h.getDayOfWeek())
                    .openTime(h.getOpenTime())
                    .closeTime(h.getCloseTime())
                    .isClosed(h.getIsClosed() != null ? h.getIsClosed() : false)
                    .build())
                .collect(Collectors.toList());
            restaurant.setOperatingHours(hours);
        }

        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Restaurant created: {} status: PENDING", saved.getId());
        return restaurantMapper.toResponse(saved);
    }

    /**
     * Get a restaurant by ID — public, anyone can view.
     */
    @Transactional(readOnly = true)
    public RestaurantDto.RestaurantResponse getRestaurantById(UUID id) {
        return restaurantMapper.toResponse(findById(id));
    }

    /**
     * Get all restaurants owned by this user.
     * Returns a List — the Angular dashboard uses restaurants[0].
     * Called by GET /api/v1/restaurants/owner
     */
    @Transactional(readOnly = true)
    public List<RestaurantDto.RestaurantResponse> getRestaurantsByOwner(String ownerUserId) {
        log.debug("Fetching restaurants for owner: {}", ownerUserId);
        return restaurantRepository.findByOwnerUserId(ownerUserId)
            .stream()
            .map(restaurantMapper::toResponse)
            .toList();
    }

    /**
     * Get the single restaurant owned by this user.
     * Called by GET /api/v1/restaurants/my (legacy endpoint — kept for compatibility)
     */
    @Transactional(readOnly = true)
    public RestaurantDto.RestaurantResponse getMyRestaurant(String ownerUserId) {
        log.debug("Fetching restaurant for owner: {}", ownerUserId);
        return restaurantRepository.findFirstByOwnerUserId(ownerUserId)
            .map(restaurantMapper::toResponse)
            .orElseThrow(() -> new RestaurantExceptions.RestaurantNotFoundException(
                "No restaurant found for this account"
            ));
    }

    /**
     * Get all active restaurants — paginated for customer browsing.
     */
    @Transactional(readOnly = true)
    public Page<RestaurantDto.RestaurantSummaryResponse> getAllActiveRestaurants(
            Pageable pageable) {
        return restaurantRepository
            .findByStatus(RestaurantStatus.ACTIVE, pageable)
            .map(restaurantMapper::toSummaryResponse);
    }

    /**
     * Search restaurants by name or cuisine type.
     */
    @Transactional(readOnly = true)
    public Page<RestaurantDto.RestaurantSummaryResponse> searchRestaurants(
            String query, Pageable pageable) {
        return restaurantRepository
            .searchActiveRestaurants(query, pageable)
            .map(restaurantMapper::toSummaryResponse);
    }

    /**
     * Update restaurant profile — owner only.
     * Verifies the caller owns the restaurant before updating.
     */
    @Transactional
    public RestaurantDto.RestaurantResponse updateRestaurant(
            UUID id,
            RestaurantDto.UpdateRestaurantRequest request,
            String ownerUserId) {

        Restaurant restaurant = findById(id);
        verifyOwnership(restaurant, ownerUserId);
        restaurantMapper.updateEntityFromRequest(request, restaurant);

        if (request.getOperatingHours() != null) {
            restaurant.getOperatingHours().clear();
            List<OperatingHours> hours = request.getOperatingHours().stream()
                .map(h -> OperatingHours.builder()
                    .restaurant(restaurant)
                    .dayOfWeek(h.getDayOfWeek())
                    .openTime(h.getOpenTime())
                    .closeTime(h.getCloseTime())
                    .isClosed(h.getIsClosed() != null ? h.getIsClosed() : false)
                    .build())
                .collect(Collectors.toList());
            restaurant.getOperatingHours().addAll(hours);
        }

        return restaurantMapper.toResponse(restaurantRepository.save(restaurant));
    }

    /**
     * Update restaurant status — ADMIN only.
     */
    @Transactional
    public RestaurantDto.RestaurantResponse updateStatus(
            UUID id,
            RestaurantDto.UpdateStatusRequest request) {

        Restaurant restaurant = findById(id);

        if (restaurant.getStatus() == RestaurantStatus.CLOSED) {
            throw new RestaurantExceptions.InvalidStatusTransitionException(
                "Cannot change status of a permanently closed restaurant"
            );
        }

        log.info("Updating restaurant {} status: {} → {}",
            id, restaurant.getStatus(), request.getStatus());

        restaurant.setStatus(request.getStatus());
        return restaurantMapper.toResponse(restaurantRepository.save(restaurant));
    }

    // =====================================================
    // Private helpers
    // =====================================================

    private Restaurant findById(UUID id) {
        return restaurantRepository.findById(id)
            .orElseThrow(() -> new RestaurantExceptions.RestaurantNotFoundException(
                "Restaurant not found with id: " + id
            ));
    }

    private void verifyOwnership(Restaurant restaurant, String ownerUserId) {
        if (!restaurant.getOwnerUserId().equals(ownerUserId)) {
            throw new RestaurantExceptions.UnauthorizedRestaurantAccessException(
                "You do not have permission to modify this restaurant"
            );
        }
    }
}
