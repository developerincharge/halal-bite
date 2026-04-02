package com.halalbite.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

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

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())

            .authorizeExchange(exchanges -> exchanges

                // ---- PUBLIC ROUTES — no JWT needed ----

                // Actuator health endpoint — needed by Docker healthcheck
                .pathMatchers("/actuator/**").permitAll()

                // Auth endpoints — user must be able to login/register
                // without a token (they don't have one yet!)
                .pathMatchers("/api/v1/auth/**").permitAll()

                // Browse restaurants and menus — public, no login required
                // Users should be able to see restaurants before signing up
                .pathMatchers("/api/v1/restaurants/**").permitAll()
                .pathMatchers("/api/v1/menus/**").permitAll()

                // Eureka dashboard (if accessed through gateway)
                .pathMatchers("/eureka/**").permitAll()

                // ---- PROTECTED ROUTES — valid JWT required ----

                // Everything else requires authentication
                .anyExchange().authenticated()
            )

            // Tell Spring Security to validate JWT tokens
            // using Keycloak as the token issuer (configured in application.yml)
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})
            );

        return http.build();
    }
}
