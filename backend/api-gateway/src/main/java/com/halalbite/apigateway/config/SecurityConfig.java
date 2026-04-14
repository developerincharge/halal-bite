package com.halalbite.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Security Configuration for API Gateway
 *
 * What this does:
 * - PUBLIC routes: no JWT needed (login, register, browse restaurants/menus)
 * - PROTECTED routes: valid JWT required (place order, view profile, pay)
 *
 * How JWT validation works:
 * 1. Customer app sends request with "Authorization: Bearer <jwt_token>" header
 * 2. This gateway intercepts the request
 * 3. It calls Keycloak (configured in application.yml) to validate the token
 * 4. If valid → forwards request to the microservice
 * 5. If invalid → returns 401 Unauthorized immediately
 *
 * The microservices themselves never deal with auth — the gateway handles it.
 * This is the "authentication at the edge" pattern.
 *
 * Why @EnableWebFluxSecurity and not @EnableWebSecurity?
 * Spring Cloud Gateway uses WebFlux (reactive/non-blocking).
 * Regular Spring MVC security (@EnableWebSecurity) does NOT work here.
 * You must use the reactive version (@EnableWebFluxSecurity).
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow Angular dev server and production domains
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",   // Angular restaurant dashboard
                "http://localhost:4201",   // Admin portal
                "http://localhost:8088",   // Customer app web ← ADD THIS
                "http://localhost:3000"    // Future use
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);      // Cache preflight for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/api/v1/auth/**").permitAll()
                        // Public restaurant browsing
                        .pathMatchers(HttpMethod.GET, "/api/v1/restaurants/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/restaurants/search").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/restaurants/{id}").permitAll()
                        // Public menu browsing
                        .pathMatchers(HttpMethod.GET, "/api/v1/menus/**").permitAll()
                        // PayPal — no JWT, uses PayPal's IPN for security instead and callbacks
                        .pathMatchers("/api/v1/payments/success").permitAll()
                        .pathMatchers("/api/v1/payments/cancel").permitAll()
                        // Everything else requires a valid JWT
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(reactiveJwtDecoder()))
                );

        return http.build();
    }
}
