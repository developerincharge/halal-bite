package com.halalbite.restaurantservice.service;

import com.halalbite.restaurantservice.dto.RestaurantDto;
import com.halalbite.restaurantservice.entity.Restaurant;
import com.halalbite.restaurantservice.entity.RestaurantStatus;
import com.halalbite.restaurantservice.exception.RestaurantExceptions;
import com.halalbite.restaurantservice.mapper.RestaurantMapper;
import com.halalbite.restaurantservice.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantService Unit Tests")
class RestaurantServiceTest {

    @Mock private RestaurantRepository restaurantRepository;
    @Mock private RestaurantMapper restaurantMapper;
    @InjectMocks private RestaurantService restaurantService;

    private Restaurant testRestaurant;
    private RestaurantDto.CreateRestaurantRequest createRequest;
    private RestaurantDto.RestaurantResponse restaurantResponse;
    private final String OWNER_KEYCLOAK_ID = "owner-keycloak-123";

    @BeforeEach
    void setUp() {
        testRestaurant = Restaurant.builder()
            .id(UUID.randomUUID())
            .ownerKeycloakId(OWNER_KEYCLOAK_ID)
            .name("Halal Burgers")
            .cuisineType("American")
            .status(RestaurantStatus.PENDING)
            .build();

        createRequest = RestaurantDto.CreateRestaurantRequest.builder()
            .name("Halal Burgers")
            .cuisineType("American")
            .phoneNumber("555-1234")
            .streetAddress("123 Main St")
            .city("Chicago")
            .postalCode("60601")
            .build();

        restaurantResponse = RestaurantDto.RestaurantResponse.builder()
            .id(testRestaurant.getId())
            .name("Halal Burgers")
            .status(RestaurantStatus.PENDING)
            .build();
    }

    @Test
    @DisplayName("createRestaurant — should create with PENDING status")
    void createRestaurant_success() {
        when(restaurantRepository.existsByOwnerKeycloakId(OWNER_KEYCLOAK_ID)).thenReturn(false);
        when(restaurantMapper.toEntity(createRequest)).thenReturn(testRestaurant);
        when(restaurantRepository.save(any())).thenReturn(testRestaurant);
        when(restaurantMapper.toResponse(testRestaurant)).thenReturn(restaurantResponse);

        RestaurantDto.RestaurantResponse result =
            restaurantService.createRestaurant(createRequest, OWNER_KEYCLOAK_ID);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RestaurantStatus.PENDING);
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    @DisplayName("createRestaurant — should throw if owner already has a restaurant")
    void createRestaurant_alreadyExists_throws() {
        when(restaurantRepository.existsByOwnerKeycloakId(OWNER_KEYCLOAK_ID)).thenReturn(true);

        assertThatThrownBy(() ->
            restaurantService.createRestaurant(createRequest, OWNER_KEYCLOAK_ID))
            .isInstanceOf(RestaurantExceptions.RestaurantAlreadyExistsException.class);

        verify(restaurantRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateRestaurant — should throw if wrong owner tries to update")
    void updateRestaurant_wrongOwner_throws() {
        when(restaurantRepository.findById(testRestaurant.getId()))
            .thenReturn(Optional.of(testRestaurant));

        assertThatThrownBy(() ->
            restaurantService.updateRestaurant(
                testRestaurant.getId(),
                new RestaurantDto.UpdateRestaurantRequest(),
                "different-owner-id"
            ))
            .isInstanceOf(RestaurantExceptions.UnauthorizedRestaurantAccessException.class);
    }

    @Test
    @DisplayName("updateStatus — should throw if restaurant is already CLOSED")
    void updateStatus_closedRestaurant_throws() {
        testRestaurant.setStatus(RestaurantStatus.CLOSED);
        when(restaurantRepository.findById(testRestaurant.getId()))
            .thenReturn(Optional.of(testRestaurant));

        RestaurantDto.UpdateStatusRequest request =
            RestaurantDto.UpdateStatusRequest.builder()
                .status(RestaurantStatus.ACTIVE)
                .build();

        assertThatThrownBy(() ->
            restaurantService.updateStatus(testRestaurant.getId(), request))
            .isInstanceOf(RestaurantExceptions.InvalidStatusTransitionException.class);
    }
}
