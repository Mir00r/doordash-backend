# Payment Service Dockerfile
# Multi-stage build for optimized production image

# Build stage
FROM gradle:8.5-jdk21-alpine AS builder

WORKDIR /app

# Copy gradle files for dependency resolution
COPY build.gradle settings.gradle gradlew ./
COPY gradle/ gradle/

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build application
RUN gradle clean build -x test --no-daemon

# Runtime stage
FROM openjdk:21-jdk-slim

# Install security updates and required packages
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y --no-install-recommends \
        curl \
        dumb-init \
        && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd -r payment && \
    useradd -r -g payment payment

# Set working directory
WORKDIR /app

# Copy application jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Create logs directory and set permissions
RUN mkdir -p logs && \
    chown -R payment:payment /app

# Switch to non-root user
USER payment

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8085/actuator/health || exit 1

# Expose port
EXPOSE 8085

# Set JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=docker"

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]

# Start application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Labels for metadata
LABEL maintainer="DoorDash Engineering <engineering@doordash.com>"
LABEL version="1.0.0"
LABEL description="DoorDash Payment Service - Secure payment processing microservice"
LABEL org.opencontainers.image.source="https://github.com/doordash/doordash-backend"
LABEL org.opencontainers.image.documentation="https://github.com/doordash/doordash-backend/tree/main/services/payment-service"
LABEL org.opencontainers.image.licenses="MIT"
