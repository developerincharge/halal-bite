# =====================================================
# Halal Bite — Spring Boot Service Dockerfile
# Multi-stage build:
#   Stage 1 (builder): Maven compiles the JAR
#   Stage 2 (runtime): JRE only, no build tools
#
# This same Dockerfile goes in EVERY backend service folder.
# Just copy it into each service directory.
# =====================================================

# ---- Stage 1: Build ----
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml first — Docker caches this layer separately.
# If only source code changed (not pom.xml), Maven doesn't
# re-download all dependencies. Saves 2-3 minutes per build.
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Now copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -q

# ---- Stage 2: Runtime ----
# Use JRE only — no compiler, no Maven. Much smaller image.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user — security best practice
# Never run production containers as root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Copy only the JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Health check — Docker and Docker Compose use this to know
# when the service is ready to receive traffic
HEALTHCHECK --interval=15s --timeout=10s --start-period=60s --retries=5 \
  CMD wget -q -O- http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1

EXPOSE 8080

# JVM tuning flags for containers:
# -XX:+UseContainerSupport  → JVM respects Docker memory limits
# -XX:MaxRAMPercentage=75   → Use max 75% of container RAM for heap
# -Djava.security.egd      → Faster startup on Linux
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-railway}", \
  "-jar", "app.jar"]
