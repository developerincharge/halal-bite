package com.halalbite.userservice.service;

import com.halalbite.userservice.dto.UserDto;
import com.halalbite.userservice.entity.User;
import com.halalbite.userservice.entity.UserAddress;
import com.halalbite.userservice.exception.GlobalExceptionHandler;
import com.halalbite.userservice.mapper.UserMapper;
import com.halalbite.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * User Service — business logic layer
 *
 * SOLID principle applied here:
 * The controller handles HTTP (what came in, what to send back).
 * The service handles BUSINESS LOGIC (what should happen).
 * The repository handles DATABASE ACCESS (how to store/retrieve).
 *
 * Why @Transactional?
 * If a method does multiple database operations and one fails,
 * @Transactional rolls back ALL of them automatically.
 * Without it, you could save a user but fail to save their address,
 * leaving your database in a broken half-saved state.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Create a new user profile after they register in Keycloak.
     *
     * Flow:
     * 1. User signs up in Keycloak (frontend handles this)
     * 2. Frontend calls POST /api/v1/users with the JWT token + profile data
     * 3. We extract the Keycloak ID from the JWT and create the profile here
     */
    @Transactional
    public UserDto.UserResponse createUser(UserDto.CreateUserRequest request, String userId) {
        log.info("Creating user profile for userId: {}", userId);

        // Check if a profile already exists for this Keycloak account
        if (userRepository.findByUserId(userId).isPresent()) {
            log.warn("User profile already exists for userId: {}", userId);
            // Return existing profile rather than throwing an error
            return userMapper.toResponse(userRepository.findByuserId(userId).get());
        }

        // Check if email is already used by another account
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setuserId(userId);

        User savedUser = userRepository.save(user);
        log.info("User profile created with id: {}", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    /**
     * Get the profile of the currently authenticated user.
     * The userId comes from the JWT token — not from the URL.
     * This prevents users from reading each other's profiles.
     */
    @Transactional(readOnly = true)
    public UserDto.UserResponse getCurrentUser(String userId) {
        log.debug("Fetching profile for userId: {}", userId);
        User user = findActiveUserByuserId(userId);
        return userMapper.toResponse(user);
    }

    /**
     * Get any user by their internal UUID.
     * Used by other microservices (e.g. order-service fetching user details).
     * TODO: Add service-to-service auth so only internal services can call this.
     */
    @Transactional(readOnly = true)
    public UserDto.UserResponse getUserById(UUID userId) {
        log.debug("Fetching user by id: {}", userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return userMapper.toResponse(user);
    }

    /**
     * Update the current user's profile.
     * Only updates fields that are actually sent (PATCH behaviour).
     * Email cannot be changed here — that is handled by Keycloak.
     */
    @Transactional
    public UserDto.UserResponse updateCurrentUser(UserDto.UpdateUserRequest request, String userId) {
        log.info("Updating profile for userId: {}", userId);
        User user = findActiveUserById(userId);
        userMapper.updateEntityFromRequest(request, user);
        return userMapper.toResponse(userRepository.save(user));
    }

    /**
     * Add a delivery address to the current user's profile.
     * If isDefault is true, all other addresses are set to non-default first.
     */
    @Transactional
    public UserDto.UserResponse addAddress(UserDto.AddressRequest request, String userId) {
        log.info("Adding address for userId: {}", userId);
        User user = findActiveUserByuserId(userId);

        // If this new address is the default, unset all existing defaults
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            user.getAddresses().forEach(addr -> addr.setIsDefault(false));
        }

        UserAddress address = UserAddress.builder()
            .user(user)
            .label(request.getLabel())
            .streetAddress(request.getStreetAddress())
            .city(request.getCity())
            .state(request.getState())
            .postalCode(request.getPostalCode())
            .country(request.getCountry() != null ? request.getCountry() : "United States")
            .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
            .build();

        user.getAddresses().add(address);
        return userMapper.toResponse(userRepository.save(user));
    }

    /**
     * Soft-delete the current user's account.
     * Sets isActive = false. Data is preserved for order history.
     */
    @Transactional
    public void deactivateCurrentUser(String userId) {
        log.info("Deactivating account for userId: {}", userId);
        User user = findActiveUserByuserId(userId);
        user.setIsActive(false);
        userRepository.save(user);
    }

    // ---- Private helpers ----

    private User findActiveUserByuserId(String userId) {
        return userRepository.findByUserIdAndIsActiveTrue(userId)
            .orElseThrow(() -> new RuntimeException("Active user not found for userId: " + userId));
    }
}
