package com.halalbite.userservice.controller;

import com.halalbite.userservice.dto.UserDto;
import com.halalbite.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * User Controller — REST API endpoints for user management
 *
 * Base URL: /api/v1/users
 *
 * How JWT authentication works here:
 * When a request arrives with "Authorization: Bearer <token>",
 * Spring Security validates the token and injects it as a Jwt object.
 * We extract the Keycloak user ID from jwt.getSubject().
 * This way we NEVER trust a user ID from the URL or request body —
 * we always use the ID from the verified JWT token.
 *
 * @AuthenticationPrincipal Jwt jwt — Spring injects the validated JWT
 * jwt.getSubject() — returns the Keycloak user ID (the "sub" claim)
 *
 * API endpoints:
 *   POST   /api/v1/users          → create user profile (after Keycloak registration)
 *   GET    /api/v1/users/me       → get current user's profile
 *   PATCH  /api/v1/users/me       → update current user's profile
 *   DELETE /api/v1/users/me       → deactivate current user's account
 *   POST   /api/v1/users/me/addresses → add a delivery address
 *   GET    /api/v1/users/{id}     → get user by ID (for internal service use)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * POST /api/v1/users
     * Create a user profile after Keycloak registration.
     * The JWT token identifies who is creating the profile.
     */
    @PostMapping
    public ResponseEntity<UserDto.UserResponse> createUser(
            @Valid @RequestBody UserDto.CreateUserRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("POST /api/v1/users — userId: {}", userId);
        UserDto.UserResponse response = userService.createUser(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/users/me
     * Get the current authenticated user's profile.
     * Uses JWT to identify the user — not a URL parameter.
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto.UserResponse> getCurrentUser(
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.debug("GET /api/v1/users/me — userId: {}", userId);
        return ResponseEntity.ok(userService.getCurrentUser(userId));
    }

    /**
     * PATCH /api/v1/users/me
     * Update the current user's profile (partial update).
     * Only sends the fields you want to change.
     */
    @PatchMapping("/me")
    public ResponseEntity<UserDto.UserResponse> updateCurrentUser(
            @Valid @RequestBody UserDto.UpdateUserRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("PATCH /api/v1/users/me — userId: {}", userId);
        return ResponseEntity.ok(userService.updateCurrentUser(request, userId));
    }

    /**
     * DELETE /api/v1/users/me
     * Soft-delete (deactivate) the current user's account.
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivateCurrentUser(
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("DELETE /api/v1/users/me — userId: {}", userId);
        userService.deactivateCurrentUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/users/me/addresses
     * Add a new delivery address for the current user.
     */
    @PostMapping("/me/addresses")
    public ResponseEntity<UserDto.UserResponse> addAddress(
            @Valid @RequestBody UserDto.AddressRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("POST /api/v1/users/me/addresses — userId: {}", userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.addAddress(request, userId));
    }

    /**
     * GET /api/v1/users/{id}
     * Get any user by their internal UUID.
     * Intended for internal service-to-service calls.
     * TODO: Restrict this endpoint to internal services only using a role check.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto.UserResponse> getUserById(@PathVariable UUID id) {
        log.debug("GET /api/v1/users/{}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
