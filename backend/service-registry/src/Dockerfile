# =====================================================
# HALAL-BITE — Service Registry Dockerfile
# =====================================================
# Multi-stage build:
#   Stage 1 (builder) — compiles the Java code into a JAR
#   Stage 2 (runtime) — runs the JAR in a minimal image
#
# Why multi-stage?
# The build tools (Maven, JDK) are only needed to compile.
# The final image only needs the JRE to run.
# This keeps the production image small and secure.
# =====================================================

# ---- Stage 1: Build ----
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first
# Docker caches this layer — if pom.xml hasn't changed,
# it won't re-download dependencies on every build
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code and build the JAR
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user for security
# Never run containers as root in production
RUN addgroup -S halalbite && adduser -S halalbite -G halalbite

# Copy only the built JAR from stage 1
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to the non-root user
RUN chown halalbite:halalbite app.jar
USER halalbite

# Expose the Eureka port
EXPOSE 8761

# Health check — Docker uses this to know when the app is ready
HEALTHCHECK --interval=15s --timeout=10s --start-period=30s --retries=10 \
    CMD wget --quiet --tries=1 --spider http://localhost:8761/actuator/health || exit 1

# Start the application
# -XX:+UseContainerSupport — tells JVM to respect Docker memory limits
# -XX:MaxRAMPercentage=75.0 — use max 75% of container memory for heap
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", "app.jar"]
