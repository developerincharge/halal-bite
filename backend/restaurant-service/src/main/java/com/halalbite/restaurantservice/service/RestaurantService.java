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
 * Key concepts demonstrated here:
 *
 * 1. Role-based access control in the service layer
 *    We check if the authenticated user owns this restaurant
 *    before allowing any modification. This is defence in depth —
 *    even if the gateway or controller is bypassed somehow,
 *    the service still enforces ownership.
 *
 * 2. Pagination for list endpoints
 *    getAllActiveRestaurants() returns Page<> not List<>
 *    This prevents loading thousands of restaurants into memory.
 *
 * 3. Status transition logic
 *    Only admins can change status. The service validates that
 *    the transition makes sense (can't go from CLOSED → ACTIVE).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;

    /**
     * Register a new restaurant.
     * Called by RESTAURANT_OWNER after they sign up.
     * Status starts as PENDING — admin must approve before customers see it.
     */
    @Transactional
    public RestaurantDto.RestaurantResponse createRestaurant(
            RestaurantDto.CreateRestaurantRequest request,
            String ownerKeycloakId) {

        log.info("Creating restaurant for owner: {}", ownerKeycloakId);

        // One owner = one restaurant for now
        // TODO: Allow multiple restaurants per owner in a future version
        if (restaurantRepository.existsByOwnerKeycloakId(ownerKeycloakId)) {
            throw new RestaurantExceptions.RestaurantAlreadyExistsException(
                "You already have a restaurant registered. Contact support to register another."
            );
        }

        Restaurant restaurant = restaurantMapper.toEntity(request);
        // Set defaults explicitly — MapStruct bypasses @Builder.Default
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
        restaurant.setOwnerKeycloakId(ownerKeycloakId);
        restaurant.setStatus(RestaurantStatus.PENDING);

        // Set affiliate revenue percentage — use request value or default 15%
        if (request.getAffiliateRevenuePercentage() != null) {
            restaurant.setAffiliateRevenuePercentage(
                request.getAffiliateRevenuePercentage()
            );
        }

        // Add operating hours if provided
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
        log.info("Restaurant created with id: {} status: PENDING", saved.getId());
        return restaurantMapper.toResponse(saved);
    }

    /**
     * Get a restaurant by ID — public endpoint, anyone can view.
     */
    @Transactional(readOnly = true)
    public RestaurantDto.RestaurantResponse getRestaurantById(UUID id) {
        Restaurant restaurant = findById(id);
        return restaurantMapper.toResponse(restaurant);
    }

    /**
     * Get the restaurant owned by the currently authenticated user.
     */
    @Transactional(readOnly = true)
    public RestaurantDto.RestaurantResponse getMyRestaurant(String ownerKeycloakId) {
        Restaurant restaurant = restaurantRepository
            .findByOwnerKeycloakId(ownerKeycloakId)
            .orElseThrow(() -> new RestaurantExceptions.RestaurantNotFoundException(
                "No restaurant found for this account"
            ));
        return restaurantMapper.toResponse(restaurant);
    }

    /**
     * Get all active restaurants — paginated, for customer browsing.
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
     */
    @Transactional
    public RestaurantDto.RestaurantResponse updateRestaurant(
            UUID id,
            RestaurantDto.UpdateRestaurantRequest request,
            String ownerKeycloakId) {

        Restaurant restaurant = findById(id);
        verifyOwnership(restaurant, ownerKeycloakId);
        restaurantMapper.updateEntityFromRequest(request, restaurant);

        // Update operating hours if provided
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
     * The controller enforces the ADMIN role — service enforces business rules.
     */
    @Transactional
    public RestaurantDto.RestaurantResponse updateStatus(
            UUID id,
            RestaurantDto.UpdateStatusRequest request) {

        Restaurant restaurant = findById(id);

        // Validate status transition
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

    // ---- Private helpers ----

    private Restaurant findById(UUID id) {
        return restaurantRepository.findById(id)
            .orElseThrow(() -> new RestaurantExceptions.RestaurantNotFoundException(
                "Restaurant not found with id: " + id
            ));
    }

    private void verifyOwnership(Restaurant restaurant, String keycloakId) {
        if (!restaurant.getOwnerKeycloakId().equals(keycloakId)) {
            throw new RestaurantExceptions.UnauthorizedRestaurantAccessException(
                "You do not have permission to modify this restaurant"
            );
        }
    }
}
