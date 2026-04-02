package com.halalbite.authservice.service;

import com.halalbite.authservice.dto.AuthDto;
import com.halalbite.authservice.entity.AuthUser;
import com.halalbite.authservice.entity.UserRole;
import com.halalbite.authservice.repository.AuthUserRepository;
import com.halalbite.authservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private AuthUserRepository authUserRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @InjectMocks private AuthService authService;

    private AuthUser testUser;
    private final String TEST_EMAIL = "test@halalbite.com";
    private final String TEST_PASSWORD = "password123";
    private final String TEST_HASH = "$2a$10$hashed";
    private final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiJ9.test.token";

    @BeforeEach
    void setUp() {
        testUser = AuthUser.builder()
            .id(UUID.randomUUID())
            .email(TEST_EMAIL)
            .passwordHash(TEST_HASH)
            .role(UserRole.CUSTOMER)
            .isActive(true)
            .build();
    }

    @Test
    @DisplayName("register — should create user and return token")
    void register_success() {
        when(authUserRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_HASH);
        when(authUserRepository.save(any())).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn(TEST_TOKEN);
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        AuthDto.RegisterRequest request = AuthDto.RegisterRequest.builder()
            .email(TEST_EMAIL).password(TEST_PASSWORD).build();

        AuthDto.AuthResponse result = authService.register(request);

        assertThat(result.getAccessToken()).isEqualTo(TEST_TOKEN);
        assertThat(result.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        verify(authUserRepository).save(any(AuthUser.class));
    }

    @Test
    @DisplayName("register — should throw if email already registered")
    void register_duplicateEmail_throws() {
        when(authUserRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
            AuthDto.RegisterRequest.builder()
                .email(TEST_EMAIL).password(TEST_PASSWORD).build()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("already registered");

        verify(authUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("login — should return token for valid credentials")
    void login_success() {
        when(authUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_HASH)).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn(TEST_TOKEN);
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        AuthDto.AuthResponse result = authService.login(
            AuthDto.LoginRequest.builder()
                .email(TEST_EMAIL).password(TEST_PASSWORD).build());

        assertThat(result.getAccessToken()).isEqualTo(TEST_TOKEN);
        assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("login — should throw for wrong password")
    void login_wrongPassword_throws() {
        when(authUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", TEST_HASH)).thenReturn(false);

        assertThatThrownBy(() -> authService.login(
            AuthDto.LoginRequest.builder()
                .email(TEST_EMAIL).password("wrongpassword").build()))
            .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("login — should throw for non-existent email")
    void login_unknownEmail_throws() {
        when(authUserRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(
            AuthDto.LoginRequest.builder()
                .email("unknown@test.com").password(TEST_PASSWORD).build()))
            .isInstanceOf(BadCredentialsException.class);
    }
}
