FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN ./mvnw dependency:go-offline -B
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S halalbite && adduser -S halalbite -G halalbite
COPY --from=builder /app/target/*.jar app.jar
RUN chown halalbite:halalbite app.jar
USER halalbite
EXPOSE 8084
HEALTHCHECK --interval=15s --timeout=10s --start-period=30s --retries=10 \
    CMD wget --quiet --tries=1 --spider http://localhost:8084/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
