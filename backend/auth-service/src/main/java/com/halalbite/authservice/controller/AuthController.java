package com.halalbite.authservice.controller;

import com.halalbite.authservice.dto.AuthDto;
import com.halalbite.authservice.security.JwtService;
import com.halalbite.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Auth Controller
 *
 * Endpoints:
 *   POST /api/v1/auth/register  → register new account, returns JWT
 *   POST /api/v1/auth/login     → login, returns JWT
 *   GET  /api/v1/auth/validate  → validate a token (used by other services)
 *   GET  /api/v1/auth/me        → decode token and return user info
 *
 * ALL endpoints here are PUBLIC — no JWT required.
 * You cannot require a token to get a token!
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    /**
     * POST /api/v1/auth/register
     *
     * Postman test:
     * Method: POST
     * URL: http://localhost:8080/api/v1/auth/register
     * Body (JSON):
     * {
     *   "email": "customer@halalbite.com",
     *   "password": "password123",
     *   "role": "CUSTOMER"
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthDto.AuthResponse> register(
            @Valid @RequestBody AuthDto.RegisterRequest request) {
        log.info("POST /api/v1/auth/register - {}", request.getEmail());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(authService.register(request));
    }

    /**
     * POST /api/v1/auth/login
     *
     * Postman test:
     * Method: POST
     * URL: http://localhost:8080/api/v1/auth/login
     * Body (JSON):
     * {
     *   "email": "customer@halalbite.com",
     *   "password": "password123"
     * }
     *
     * Copy the accessToken from the response and use it as:
     * Authorization: Bearer <accessToken>
     */
    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponse> login(
            @Valid @RequestBody AuthDto.LoginRequest request) {
        log.info("POST /api/v1/auth/login - {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * GET /api/v1/auth/validate?token=<jwt>
     *
     * Used internally by other services to check if a token is valid.
     * Returns 200 with user info if valid, 401 if invalid/expired.
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestParam String token) {
        if (!jwtService.isTokenValid(token)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("valid", false, "message", "Token is invalid or expired"));
        }

        List<String> roles = jwtService.extractRoles(token);
        return ResponseEntity.ok(Map.of(
            "valid",  true,
            "userId", jwtService.extractUserId(token),
            "email",  jwtService.extractEmail(token),
            "roles",  roles
        ));
    }

    /**
     * GET /api/v1/auth/me
     * Header: Authorization: Bearer <token>
     *
     * Decodes the token from the Authorization header and returns user info.
     * Useful for the frontend to get user details from the token.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        if (!jwtService.isTokenValid(token)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Token is invalid or expired"));
        }

        return ResponseEntity.ok(Map.of(
            "userId", jwtService.extractUserId(token),
            "email",  jwtService.extractEmail(token),
            "roles",  jwtService.extractRoles(token)
        ));
    }
}
