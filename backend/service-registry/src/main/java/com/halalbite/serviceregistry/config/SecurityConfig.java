package com.halalbite.serviceregistry.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for Service Registry
 *
 * Why do we need this?
 * Spring Security is on the classpath (we added it as a dependency).
 * By default it locks down EVERY endpoint and requires login.
 * That would block other microservices from registering with Eureka.
 *
 * This config:
 * - Allows all requests to the Eureka dashboard and registration endpoints
 * - Disables CSRF (not needed for a service registry)
 *
 * TODO: In production, re-enable authentication so only your own
 *       services can register — not anyone on the internet.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for service-to-service communication
            .csrf(csrf -> csrf.disable())
            // Allow all requests — Eureka clients need to register freely in dev
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
