package com.halalbite.authservice.security;

import com.halalbite.authservice.entity.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT Service — creates and validates JWT tokens
 *
 * What is a JWT token?
 * A JSON Web Token has 3 parts separated by dots:
 *   header.payload.signature
 *
 * Header: algorithm used (HS256)
 * Payload: the claims (user data embedded in the token)
 * Signature: cryptographic proof the token wasn't tampered with
 *
 * The token is signed with a SECRET KEY that only your server knows.
 * When a service receives a token, it verifies the signature using
 * the same key. If it matches → token is valid and trusted.
 * If someone tampers with the payload → signature check fails → 401.
 *
 * Token payload (claims) for halal-bite:
 * {
 *   "sub":   "uuid-of-the-user",
 *   "email": "user@example.com",
 *   "roles": ["CUSTOMER"],
 *   "iat":   1234567890,  ← issued at
 *   "exp":   1234654290   ← expires at (24 hours later)
 * }
 *
 * Why 24 hours expiry?
 * Long enough for a full day's work without re-logging in.
 * Short enough that a stolen token has limited damage window.
 */
@Slf4j
@Service
public class JwtService {

    // Secret key from application.yml — must be at least 32 characters
    @Value("${jwt.secret}")
    private String secretKey;

    // Token validity — 24 hours in milliseconds
    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    /**
     * Generate a JWT token for a given user.
     * Called after successful registration or login.
     */
    public String generateToken(AuthUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("roles", List.of(user.getRole().name()));
        // "sub" = subject = the user's unique ID across all services

        return Jwts.builder()
            .claims(claims)
            .subject(user.getId().toString())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * Extract the user ID (sub claim) from a token.
     * Other services call this to identify the authenticated user.
     */
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extract the email claim from a token.
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    /**
     * Extract roles from the token.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractAllClaims(token).get("roles", List.class);
    }

    /**
     * Check if a token has expired.
     */
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * Validate a token — checks signature and expiry.
     * Returns true if token is valid, false if invalid or expired.
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get expiration time in seconds (for the response body).
     */
    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    // ---- Private helpers ----

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey getSigningKey() {
        // Convert the secret string to a proper cryptographic key
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
