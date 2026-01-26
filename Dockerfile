# Multi-stage Dockerfile for building and running the Spring Boot application

# 1) Build stage: build the WAR using Maven
FROM maven:3.9.5-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copy pom and download dependencies first (utilizes Docker layer cache)
COPY pom.xml ./
# If you have additional top-level files that affect the build (settings.xml etc.) copy them here

# Copy the source
COPY src ./src

# Build the application (skip tests to make image build faster; remove -DskipTests for CI builds)
RUN mvn -B -DskipTests package


# 2) Runtime stage: run the produced WAR with a JRE (slim)
FROM eclipse-temurin:17-jre-jammy

# Install curl and netcat for healthcheck and startup checks, and cleanup apt lists in same layer to reduce image size
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates netcat \
    && rm -rf /var/lib/apt/lists/*

# Create runtime user and app directory in one layer
RUN useradd --create-home --no-log-init appuser \
    && mkdir -p /app /app/config \
    && chown -R appuser:appuser /app
WORKDIR /app

# Copy the startup script
COPY docker/start.sh /usr/local/bin/start.sh
RUN chmod +x /usr/local/bin/start.sh

# Copy the built WAR from the builder stage. Adjust filename if your artifactId/version differ.
COPY --from=builder /workspace/target/api-0.0.1-SNAPSHOT.war ./app.war

EXPOSE 8080

# Use an unprivileged user
USER appuser

# Healthcheck uses the actuator health endpoint; ensure actuator is enabled in your config
HEALTHCHECK --interval=30s --start-period=10s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Start script waits for DB and Redis, then runs the JAR. Accepts JVM_OPTS and additional java args.
ENTRYPOINT ["/usr/local/bin/start.sh"]
