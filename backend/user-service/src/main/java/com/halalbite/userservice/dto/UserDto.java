package com.halalbite.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTOs — Data Transfer Objects for User Service
 *
 * Why DTOs instead of sending the Entity directly?
 *
 * 1. SECURITY — Entity has fields you never want to expose (keycloakId, isActive)
 * 2. CONTROL — You decide exactly what fields the API returns
 * 3. VALIDATION — Validation annotations live here, not on the database entity
 * 4. FLEXIBILITY — API shape can change without changing the database schema
 *
 * Pattern used:
 *   CreateUserRequest  → what the client sends to CREATE a user
 *   UpdateUserRequest  → what the client sends to UPDATE a user
 *   UserResponse       → what the API always returns (never the entity itself)
 *   AddressRequest     → what the client sends for an address
 *   AddressResponse    → what the API returns for an address
 */
public class UserDto {

    // =====================================================
    // REQUEST DTOs — incoming data from the client
    // =====================================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateUserRequest {

        @NotBlank(message = "First name is required")
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        private String lastName;

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        private String email;

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        private String phoneNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateUserRequest {

        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        private String firstName;

        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        private String lastName;

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        private String phoneNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressRequest {

        @Size(max = 50)
        private String label;

        @NotBlank(message = "Street address is required")
        private String streetAddress;

        @NotBlank(message = "City is required")
        private String city;

        private String state;

        @NotBlank(message = "Postal code is required")
        private String postalCode;

        private String country;
        private Boolean isDefault;
    }

    // =====================================================
    // RESPONSE DTOs — outgoing data to the client
    // =====================================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private List<AddressResponse> addresses;
        private LocalDateTime createdAt;
        // NOTE: keycloakId and isActive are intentionally NOT included
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressResponse {
        private UUID id;
        private String label;
        private String streetAddress;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private Boolean isDefault;
    }
}
