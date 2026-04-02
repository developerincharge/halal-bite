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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Restaurant Controller
 *
 * Endpoints:
 *   POST   /api/v1/restaurants              → register restaurant (RESTAURANT_OWNER)
 *   GET    /api/v1/restaurants              → list active restaurants (PUBLIC)
 *   GET    /api/v1/restaurants/search       → search restaurants (PUBLIC)
 *   GET    /api/v1/restaurants/my           → get owner's own restaurant
 *   GET    /api/v1/restaurants/{id}         → get restaurant by ID (PUBLIC)
 *   PATCH  /api/v1/restaurants/{id}         → update restaurant (OWNER only)
 *   PATCH  /api/v1/restaurants/{id}/status  → update status (ADMIN only)
 *
 * Role checking:
 * @PreAuthorize reads the "roles" claim from the Keycloak JWT token.
 * Keycloak must be configured to include roles in the token.
 * We'll set this up in Keycloak when we configure the realm.
 */
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

        String ownerKeycloakId = jwt.getSubject();
        log.info("POST /api/v1/restaurants — owner: {}", ownerKeycloakId);
        RestaurantDto.RestaurantResponse response =
            restaurantService.createRestaurant(request, ownerKeycloakId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/restaurants
     * List all active restaurants — public, paginated.
     * Example: GET /api/v1/restaurants?page=0&size=20&sort=name
     */
    @GetMapping
    public ResponseEntity<Page<RestaurantDto.RestaurantSummaryResponse>> getAllRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(
            restaurantService.getAllActiveRestaurants(pageable)
        );
    }

    /**
     * GET /api/v1/restaurants/search?query=halal&page=0&size=20
     * Search restaurants by name or cuisine type — public.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<RestaurantDto.RestaurantSummaryResponse>> searchRestaurants(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
            restaurantService.searchRestaurants(query, pageable)
        );
    }

    /**
     * GET /api/v1/restaurants/my
     * Get the restaurant belonging to the authenticated owner.
     */
    @GetMapping("/my")
    public ResponseEntity<RestaurantDto.RestaurantResponse> getMyRestaurant(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
            restaurantService.getMyRestaurant(jwt.getSubject())
        );
    }

    /**
     * GET /api/v1/restaurants/{id}
     * Get a single restaurant by ID — public.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDto.RestaurantResponse> getRestaurantById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    /**
     * PATCH /api/v1/restaurants/{id}
     * Update restaurant profile — owner only.
     * Service layer verifies the JWT owner matches the restaurant owner.
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
     * TODO: Uncomment @PreAuthorize once Keycloak realm roles are configured.
     */
    @PatchMapping("/{id}/status")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestaurantDto.RestaurantResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantDto.UpdateStatusRequest request) {

        log.info("PATCH /api/v1/restaurants/{}/status → {}", id, request.getStatus());
        return ResponseEntity.ok(restaurantService.updateStatus(id, request));
    }
}
