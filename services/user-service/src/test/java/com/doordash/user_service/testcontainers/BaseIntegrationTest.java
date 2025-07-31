package com.doordash.user_service.testcontainers;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests that require Docker containers.
 * Provides PostgreSQL, Redis, and Kafka containers for testing.
 * 
 * This class follows the singleton pattern to reuse containers
 * across multiple test classes for better performance.
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@Testcontainers
public abstract class BaseIntegrationTest {

    /**
     * PostgreSQL container for database testing.
     * Uses the latest PostgreSQL 15 image with predefined credentials.
     */
    @Container
    static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("user_service_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    /**
     * Redis container for caching and session testing.
     * Uses Redis 7 Alpine image for smaller footprint.
     */
    @Container
    static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withReuse(true);

    /**
     * Kafka container for event-driven testing.
     * Uses Confluent Platform Kafka for reliability.
     */
    @Container
    static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .withReuse(true);

    /**
     * Configures Spring properties dynamically based on container ports.
     * This method is called before Spring context initialization.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Database configuration
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        
        // JPA configuration for testing
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
        
        // Redis configuration
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
        registry.add("spring.cache.type", () -> "redis");
        
        // Kafka configuration
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "user-service-test");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        
        // Disable external service calls in tests
        registry.add("app.auth-service.enabled", () -> "false");
        registry.add("app.notification-service.enabled", () -> "false");
        registry.add("app.file-storage.enabled", () -> "false");
        
        // Security configuration for testing
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "http://localhost:8080");
        registry.add("app.security.jwt.public-key", () -> "test-key");
        
        // Actuator configuration
        registry.add("management.endpoints.web.exposure.include", () -> "health,info,metrics");
    }

    /**
     * Utility method to get PostgreSQL container for direct database operations.
     */
    protected static PostgreSQLContainer<?> getPostgresContainer() {
        return POSTGRES_CONTAINER;
    }

    /**
     * Utility method to get Redis container for cache operations.
     */
    protected static GenericContainer<?> getRedisContainer() {
        return REDIS_CONTAINER;
    }

    /**
     * Utility method to get Kafka container for event testing.
     */
    protected static KafkaContainer getKafkaContainer() {
        return KAFKA_CONTAINER;
    }
}
