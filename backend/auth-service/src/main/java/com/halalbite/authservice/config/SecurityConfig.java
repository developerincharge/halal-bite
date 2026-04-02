package com.halalbite.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Config for Auth Service
 *
 * All endpoints in auth-service are PUBLIC — you can't require a
 * JWT to get a JWT! So we permit all requests here.
 *
 * The heavy lifting happens in JwtService (token creation/validation)
 * and AuthService (BCrypt password verification).
 *
 * The PasswordEncoder bean uses BCrypt with strength 10.
 * Strength 10 means 2^10 = 1024 rounds of hashing.
 * This makes brute-force attacks computationally expensive.
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
                .anyRequest().permitAll()
            );

        return http.build();
    }

    /**
     * BCrypt password encoder — used throughout the app to hash and verify passwords.
     * Declared as a @Bean so it can be @Autowired anywhere.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
