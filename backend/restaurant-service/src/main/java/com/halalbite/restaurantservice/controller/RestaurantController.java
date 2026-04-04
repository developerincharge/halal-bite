package com.halalbite.restaurantservice.controller;

import com.halalbite.restaurantservice.dto.RestaurantDto;
import com.halalbite.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    /**
     * POST /api/v1/restaurants
     * Register a new restaurant — RESTAURANT_OWNER only.
     */
    @PostMapping
    public ResponseEntity<RestaurantDto.RestaurantResponse> createRestaurant(
            @Valid @RequestBody RestaurantDto.CreateRestaurantRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String ownerUserId = jwt.getSubject();
        log.info("POST /api/v1/restaurants — owner: {}", ownerUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.createRestaurant(request, ownerUserId));
    }

    /**
     * GET /api/v1/restaurants
     * List all active restaurants — PUBLIC, paginated.
     */
    @GetMapping
    public ResponseEntity<Page<RestaurantDto.RestaurantSummaryResponse>> getAllRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(restaurantService.getAllActiveRestaurants(pageable));
    }

    /**
     * GET /api/v1/restaurants/search?query=halal
     * Search restaurants — PUBLIC.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<RestaurantDto.RestaurantSummaryResponse>> searchRestaurants(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(restaurantService.searchRestaurants(query, pageable));
    }

    /**
     * GET /api/v1/restaurants/owner
     * Get all restaurants owned by the authenticated user.
     * Returns a List — frontend checks restaurants[0] to get the first restaurant.
     * The Angular dashboard calls this on startup.
     */
    @GetMapping("/owner")
    public ResponseEntity<List<RestaurantDto.RestaurantResponse>> getMyRestaurants(
            @AuthenticationPrincipal Jwt jwt) {

        String ownerUserId = jwt.getSubject();
        log.info("GET /api/v1/restaurants/owner — userId: {}", ownerUserId);
        return ResponseEntity.ok(restaurantService.getRestaurantsByOwner(ownerUserId));
    }

    /**
     * GET /api/v1/restaurants/{id}
     * Get a single restaurant by ID — PUBLIC.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDto.RestaurantResponse> getRestaurantById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    /**
     * PATCH /api/v1/restaurants/{id}
     * Update restaurant profile — owner only.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<RestaurantDto.RestaurantResponse> updateRestaurant(
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantDto.UpdateRestaurantRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                restaurantService.updateRestaurant(id, request, jwt.getSubject())
        );
    }

    /**
     * PATCH /api/v1/restaurants/{id}/status
     * Update restaurant status — ADMIN only.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<RestaurantDto.RestaurantResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantDto.UpdateStatusRequest request) {

        log.info("PATCH /api/v1/restaurants/{}/status → {}", id, request.getStatus());
        return ResponseEntity.ok(restaurantService.updateStatus(id, request));
    }
}
