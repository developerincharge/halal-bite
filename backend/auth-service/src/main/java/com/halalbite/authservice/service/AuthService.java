package com.halalbite.authservice.service;

import com.halalbite.authservice.dto.AuthDto;
import com.halalbite.authservice.entity.AuthUser;
import com.halalbite.authservice.entity.UserRole;
import com.halalbite.authservice.repository.AuthUserRepository;
import com.halalbite.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Auth Service — handles registration and login logic
 *
 * Flow for REGISTER:
 * 1. Check email not already registered
 * 2. Hash the password using BCrypt
 * 3. Save AuthUser to database
 * 4. Generate JWT token
 * 5. Return token + user info
 *
 * Flow for LOGIN:
 * 1. Find user by email
 * 2. Verify password using BCrypt (compares plain → hash)
 * 3. Generate new JWT token
 * 4. Return token + user info
 *
 * Why BCrypt for passwords?
 * BCrypt is a one-way hashing algorithm designed specifically for passwords.
 * Even if your database is stolen, attackers can't reverse the hash to get
 * the original password. BCrypt also adds a "salt" (random data) so two
 * users with the same password get different hashes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Register a new user account.
     */
    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        if (authUserRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                "Email already registered: " + request.getEmail()
            );
        }

        // Default role is CUSTOMER if not specified
        UserRole role = request.getRole() != null
            ? request.getRole()
            : UserRole.CUSTOMER;

        AuthUser user = AuthUser.builder()
            .email(request.getEmail())
            // passwordEncoder.encode() runs BCrypt — never store plain text!
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(role)
            .build();

        AuthUser savedUser = authUserRepository.save(user);
        log.info("User registered: {} with role: {}", savedUser.getId(), role);

        String token = jwtService.generateToken(savedUser);

        return AuthDto.AuthResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .expiresIn(jwtService.getExpirationSeconds())
            .userId(savedUser.getId())
            .email(savedUser.getEmail())
            .role(savedUser.getRole())
            .build();
    }

    /**
     * Login with email and password — returns a JWT token.
     */
    @Transactional(readOnly = true)
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        AuthUser user = authUserRepository.findByEmail(request.getEmail())
            .orElseThrow(() ->
                // Generic message — don't reveal whether email exists
                new BadCredentialsException("Invalid email or password")
            );

        if (!user.getIsActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        // passwordEncoder.matches() compares the plain password against
        // the stored BCrypt hash — returns true if they match
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        log.info("Successful login for: {} role: {}", user.getId(), user.getRole());
        String token = jwtService.generateToken(user);

        return AuthDto.AuthResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .expiresIn(jwtService.getExpirationSeconds())
            .userId(user.getId())
            .email(user.getEmail())
            .role(user.getRole())
            .build();
    }
}
