package com.halalbite.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS Configuration for API Gateway
 *
 * What is CORS?
 * Browsers block requests from one origin (domain) to another by default.
 * For example: your React Native web preview at localhost:3000 trying to
 * call the gateway at localhost:8080 would be blocked.
 *
 * CORS (Cross-Origin Resource Sharing) tells the browser:
 * "These specific origins ARE allowed to call this API."
 *
 * This config allows:
 * - React Native customer app (localhost:8081)
 * - Angular restaurant dashboard (localhost:4200)
 * - Next.js admin portal (localhost:3000)
 *
 * TODO: In production, replace localhost origins with your real domains
 * e.g. https://app.halalbite.com, https://admin.halalbite.com
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Allowed origins — your frontend apps
        corsConfig.setAllowedOrigins(List.of(
            "http://localhost:3000",   // Next.js admin portal
            "http://localhost:4200",   // Angular restaurant dashboard
            "http://localhost:8081",   // React Native / Expo web
            "http://localhost:19006"   // Expo web alternate port
        ));

        // Allowed HTTP methods
        corsConfig.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Allowed headers — Authorization is critical for JWT
        corsConfig.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With"
        ));

        // Allow cookies and auth headers to be sent
        corsConfig.setAllowCredentials(true);

        // Cache preflight response for 1 hour (reduces OPTIONS requests)
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this CORS config to ALL routes
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
