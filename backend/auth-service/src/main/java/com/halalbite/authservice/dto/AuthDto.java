package com.halalbite.authservice.dto;

import com.halalbite.authservice.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * Auth DTOs
 *
 * RegisterRequest  → POST /api/v1/auth/register
 * LoginRequest     → POST /api/v1/auth/login
 * AuthResponse     → returned after register or login (contains the JWT)
 * RefreshRequest   → POST /api/v1/auth/refresh
 */
public class AuthDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        // Optional — defaults to CUSTOMER if not provided
        // RESTAURANT_OWNER and ADMIN set this explicitly
        private UserRole role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String accessToken;
        private String tokenType;      // Always "Bearer"
        private Long expiresIn;        // Seconds until token expires
        private UUID userId;           // The user's UUID (sub claim)
        private String email;
        private UserRole role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshRequest {
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;
    }
}
