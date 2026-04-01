# service-registry

Eureka Service Registry for the halal-bite platform.

## What it does

Every microservice in halal-bite registers itself here on startup.
Other services ask this registry to find the location of a service
instead of hardcoding IP addresses and ports.

## Port

`8761`

## Dashboard

Once running, open: http://localhost:8761

You will see all registered services listed on the Eureka dashboard.
As you build and start more services (user-service, order-service etc.),
they will appear here automatically.

## How to run locally (without Docker)

```bash
cd backend/service-registry
./mvnw spring-boot:run
```

## How to run with Docker

```bash
# From the halal-bite root:
docker compose up -d service-registry
```

## How to build and test

```bash
# Run tests
./mvnw test

# Build JAR
./mvnw clean package

# Build Docker image
docker build -t halal-bite/service-registry:local .
```

## Start order

This service must start FIRST before any other microservice.
The docker-compose.yml `depends_on` configuration enforces this.

## Tech stack

- Java 21
- Spring Boot 3.3
- Spring Cloud Netflix Eureka Server
- Spring Boot Actuator
