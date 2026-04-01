package com.halalbite.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * API Gateway — Application Context Test
 *
 * Verifies the Spring application context loads correctly
 * with all routes, filters, and security config wired up.
 *
 * We override the Keycloak issuer-uri in tests because
 * Keycloak is not running during unit tests — we don't
 * want tests to fail just because auth server is unavailable.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/halal-bite",
    "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8180/realms/halal-bite/protocol/openid-connect/certs",
    "eureka.client.enabled=false",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false"
})
class ApiGatewayApplicationTest {

    @Test
    void contextLoads() {
        // Context loads without errors — all routes, filters,
        // and security config are correctly configured
    }
}
