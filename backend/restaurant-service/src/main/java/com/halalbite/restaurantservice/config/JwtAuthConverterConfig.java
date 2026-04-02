package com.halalbite.restaurantservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Converter for user-service
 *
 * Why do we need this?
 * Our JWT token has this structure:
 * {
 *   "sub": "user-uuid",
 *   "roles": ["CUSTOMER"],    ← our custom claim
 *   "email": "user@..."
 * }
 *
 * Spring Security by default looks for roles in "scope" or "authorities".
 * We need to tell it to look in our "roles" claim instead.
 * This converter reads the "roles" array and converts each value
 * into a Spring Security GrantedAuthority with the "ROLE_" prefix.
 *
 * So "CUSTOMER" becomes "ROLE_CUSTOMER" which is what
 * @PreAuthorize("hasRole('CUSTOMER')") checks for.
 */
@Configuration
public class JwtAuthConverterConfig {

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new RolesClaimConverter());
        return converter;
    }

    static class RolesClaimConverter
            implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            // Read the "roles" claim from the token
            List<String> roles = jwt.getClaimAsStringList("roles");

            if (roles == null) {
                return List.of();
            }

            // Convert each role string to a Spring Security GrantedAuthority
            // "CUSTOMER" → SimpleGrantedAuthority("ROLE_CUSTOMER")
            return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        }
    }
}
