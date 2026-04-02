package com.halalbite.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for User Service
 *
 * This service uses STATELESS JWT authentication.
 * No sessions, no cookies — just validate the JWT on every request.
 *
 * SessionCreationPolicy.STATELESS means:
 * Spring never creates or uses an HTTP session.
 * Every request must carry its own JWT token.
 * This is the correct pattern for microservices.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Allow health checks without a token (Docker + monitoring)
                .requestMatchers("/actuator/**").permitAll()
                // GET /api/v1/users/{id} — allow for service-to-service calls
                // TODO: Lock this down to internal services only with a role
                .requestMatchers(HttpMethod.GET, "/api/v1/users/{id}").permitAll()
                // Everything else requires a valid JWT token
                .anyRequest().authenticated()
            )
            // Validate JWT tokens issued by Keycloak
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})
            );

        return http.build();
    }
}
