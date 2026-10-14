# ─── Production Dockerfile ────────────────────────────────────────────────────
# Multi-stage build: compile → runtime image

# Stage 1: Build with Maven
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
# Download dependencies first for layer caching
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q
COPY src src
RUN ./mvnw package -DskipTests -q

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Expose the configurable port
EXPOSE 8080

# DB credentials injected at runtime via environment variables
# DB_URL, DB_USERNAME, DB_PASSWORD are mandatory
ENTRYPOINT ["java", "-jar", "app.jar"]
