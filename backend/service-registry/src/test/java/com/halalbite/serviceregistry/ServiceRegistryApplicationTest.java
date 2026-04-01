package com.halalbite.serviceregistry;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Service Registry — Application Context Test
 *
 * What this tests:
 * - The Spring application context loads without errors
 * - All beans are wired correctly
 * - Eureka server configuration is valid
 *
 * This is the most fundamental test — if this fails, nothing else works.
 * Run this after any configuration change to catch problems early.
 */
@SpringBootTest
@TestPropertySource(properties = {
    // Use a random port in tests so they don't conflict with a running instance
    "server.port=0",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false"
})
class ServiceRegistryApplicationTest {

    @Test
    void contextLoads() {
        // If the Spring context starts without throwing an exception,
        // this test passes. Simple but powerful — catches misconfigurations
        // like missing beans, bad YAML, or wiring errors immediately.
    }
}
