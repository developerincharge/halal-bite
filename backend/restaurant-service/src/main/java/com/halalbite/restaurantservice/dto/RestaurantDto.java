package com.halalbite.restaurantservice.dto;

import com.halalbite.restaurantservice.entity.RestaurantStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Restaurant DTOs
 *
 * CreateRestaurantRequest — sent by RESTAURANT_OWNER when registering
 * UpdateRestaurantRequest — sent by RESTAURANT_OWNER to update profile
 * UpdateStatusRequest     — sent by ADMIN to approve/suspend/close
 * RestaurantResponse      — returned for all read operations
 * RestaurantSummaryResponse — lightweight version for listing (no hours)
 */
public class RestaurantDto {

    // =====================================================
    // REQUEST DTOs
    // =====================================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRestaurantRequest {

        @NotBlank(message = "Restaurant name is required")
        @Size(max = 200)
        private String name;

        @Size(max = 2000)
        private String description;

        @NotBlank(message = "Cuisine type is required")
        private String cuisineType;

        @NotBlank(message = "Phone number is required")
        private String phoneNumber;

        @Email(message = "Must be a valid email")
        private String email;

        @NotBlank(message = "Street address is required")
        private String streetAddress;

        @NotBlank(message = "City is required")
        private String city;

        private String state;

        @NotBlank(message = "Postal code is required")
        private String postalCode;

        private String country;

        // Optional — default 15% applied if not provided
        @DecimalMin(value = "0.0", message = "Revenue percentage cannot be negative")
        @DecimalMax(value = "1.0", message = "Revenue percentage cannot exceed 100%")
        private BigDecimal affiliateRevenuePercentage;

        @DecimalMin(value = "0.0")
        private BigDecimal minimumOrderAmount;

        private Integer estimatedDeliveryMinutes;
        private Boolean isHalalCertified;
        private String halalCertificationNumber;
        private List<OperatingHoursRequest> operatingHours;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRestaurantRequest {
        private String name;
        private String description;
        private String cuisineType;
        private String phoneNumber;
        private String email;
        private String streetAddress;
        private String city;
        private String state;
        private String postalCode;
        private String logoUrl;
        private BigDecimal minimumOrderAmount;
        private Integer estimatedDeliveryMinutes;
        private Boolean isHalalCertified;
        private String halalCertificationNumber;
        private List<OperatingHoursRequest> operatingHours;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStatusRequest {

        @NotNull(message = "Status is required")
        private RestaurantStatus status;

        private String reason; // Why the status changed — for audit trail
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatingHoursRequest {
        private DayOfWeek dayOfWeek;
        private LocalTime openTime;
        private LocalTime closeTime;
        private Boolean isClosed;
    }

    // =====================================================
    // RESPONSE DTOs
    // =====================================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantResponse {
        private UUID id;
        private String name;
        private String description;
        private String cuisineType;
        private String phoneNumber;
        private String email;
        private String logoUrl;
        private String streetAddress;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private BigDecimal minimumOrderAmount;
        private Integer estimatedDeliveryMinutes;
        private BigDecimal averageRating;
        private Integer totalReviews;
        private Boolean isHalalCertified;
        private RestaurantStatus status;
        private List<OperatingHoursResponse> operatingHours;
        private LocalDateTime createdAt;
        // NOTE: affiliateRevenuePercentage intentionally excluded
        // This is internal business data — not for public API responses
    }

    // Lightweight version used for listing — no operating hours
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantSummaryResponse {
        private UUID id;
        private String name;
        private String cuisineType;
        private String logoUrl;
        private String city;
        private BigDecimal minimumOrderAmount;
        private Integer estimatedDeliveryMinutes;
        private BigDecimal averageRating;
        private Boolean isHalalCertified;
        private RestaurantStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatingHoursResponse {
        private DayOfWeek dayOfWeek;
        private LocalTime openTime;
        private LocalTime closeTime;
        private Boolean isClosed;
    }
}
