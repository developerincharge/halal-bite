package com.halalbite.serviceregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * HALAL-BITE Service Registry
 *
 * What this does:
 * - @EnableEurekaServer turns this Spring Boot app into a Eureka registry
 * - Every other microservice (user-service, order-service, etc.) registers
 *   itself here when it starts up
 * - Services then ask this registry "where is user-service running?"
 *   instead of hardcoding IP addresses
 *
 * Think of it like a phone book — services register their address here,
 * and other services look up the address when they need to call each other.
 *
 * This MUST start before any other microservice.
 * Access the dashboard at: http://localhost:8761
 */
@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
