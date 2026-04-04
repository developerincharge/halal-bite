package com.halalbite.userservice.service;

import com.halalbite.userservice.dto.UserDto;
import com.halalbite.userservice.entity.User;
import com.halalbite.userservice.mapper.UserMapper;
import com.halalbite.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 *
 * What are unit tests?
 * Tests that test ONE unit of code (a class) in complete isolation.
 * We use Mockito to "mock" (fake) dependencies like the repository,
 * so we test ONLY the service logic — not the database.
 *
 * @ExtendWith(MockitoExtension.class) — enables Mockito in JUnit 5
 * @Mock — creates a fake version of the dependency
 * @InjectMocks — creates the real service, injecting the mocks
 *
 * Pattern used: Arrange → Act → Assert (AAA)
 *   Arrange: set up the test data and mock behaviour
 *   Act:     call the method being tested
 *   Assert:  verify the result is what we expected
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDto.CreateUserRequest createRequest;
    private UserDto.UserResponse userResponse;
    private final String TEST_USER_ID = "user-test-id-123";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(UUID.randomUUID())
            .userId(TEST_USER_ID)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .isActive(true)
            .build();

        createRequest = UserDto.CreateUserRequest.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

        userResponse = UserDto.UserResponse.builder()
            .id(testUser.getId())
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();
    }

    @Test
    @DisplayName("createUser — should create new user profile successfully")
    void createUser_success() {
        // Arrange
        when(userRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(userMapper.toEntity(createRequest)).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);

        // Act
        UserDto.UserResponse result = userService.createUser(createRequest, TEST_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("createUser — should return existing profile if already created")
    void createUser_alreadyExists_returnsExisting() {
        // Arrange — user already exists
        when(userRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);

        // Act
        UserDto.UserResponse result = userService.createUser(createRequest, TEST_USER_ID);

        // Assert — save should NEVER be called if user exists
        assertThat(result).isNotNull();
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("getCurrentUser — should return user profile for valid UserId")
    void getCurrentUser_success() {
        // Arrange
        when(userRepository.findByUserIdAndIsActiveTrue(TEST_USER_ID))
            .thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);

        // Act
        UserDto.UserResponse result = userService.getCurrentUser(TEST_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("getCurrentUser — should throw exception when user not found")
    void getCurrentUser_notFound_throwsException() {
        // Arrange
        when(userRepository.findByUserIdAndIsActiveTrue(TEST_USER_ID))
            .thenReturn(Optional.empty());

        // Act + Assert — verify the exception is thrown
        assertThatThrownBy(() -> userService.getCurrentUser(TEST_USER_ID))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Active user not found");
    }

    @Test
    @DisplayName("deactivateCurrentUser — should set isActive to false")
    void deactivateCurrentUser_success() {
        // Arrange
        when(userRepository.findByUserIdAndIsActiveTrue(TEST_USER_ID))
            .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.deactivateCurrentUser(TEST_USER_ID);

        // Assert — verify save was called and isActive was set to false
        verify(userRepository, times(1)).save(argThat(user -> !user.getIsActive()));
    }
}
