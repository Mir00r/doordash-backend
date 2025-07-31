package com.doordash.user_service.testcontainers;

import com.doordash.user_service.config.EnhancedTestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.util.stream.Stream;

/**
 * Enhanced base class for integration tests using Testcontainers.
 * 
 * This class provides:
 * - PostgreSQL database container for data persistence testing
 * - Redis container for caching integration testing  
 * - Kafka container for event-driven architecture testing
 * - Shared container instances for performance optimization
 * - Dynamic property configuration for seamless integration
 * - Common test utilities and helpers
 * 
 * Features:
 * - Container reuse for faster test execution
 * - Parallel container startup for reduced initialization time
 * - Health check validation before test execution
 * - Comprehensive logging for debugging
 * - Memory and resource optimization
 * 
 * @author DoorDash Backend Team
 * @version 2.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles({"test", "integration"})
@Import(EnhancedTestConfig.class)
public abstract class EnhancedBaseIntegrationTest {

    // Static containers for reuse across test classes
    protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER;
    protected static final RedisContainer REDIS_CONTAINER;
    protected static final KafkaContainer KAFKA_CONTAINER;
    
    // Docker images with specific versions for consistency
    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:15.4-alpine");
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7.2-alpine");
    private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:7.4.0");

    static {
        // Initialize containers with optimized configurations
        POSTGRES_CONTAINER = new PostgreSQLContainer<>(POSTGRES_IMAGE)
                .withDatabaseName("user_service_integration_test")
                .withUsername("integration_test_user")
                .withPassword("integration_test_password")
                .withReuse(true)
                .withCommand("postgres", "-c", "fsync=off", "-c", "synchronous_commit=off")
                .withTmpFs("/var/lib/postgresql/data:rw");

        REDIS_CONTAINER = new RedisContainer(REDIS_IMAGE)
                .withReuse(true)
                .withCommand("redis-server", "--appendonly", "no", "--save", "");

        KAFKA_CONTAINER = new KafkaContainer(KAFKA_IMAGE)
                .withReuse(true)
                .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
                .withEnv("KAFKA_NUM_PARTITIONS", "1")
                .withEnv("KAFKA_DEFAULT_REPLICATION_FACTOR", "1");
    }

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Start all containers in parallel before any tests run.
     */
    @BeforeAll
    static void startContainers() {
        // Start all containers in parallel for faster initialization
        Startables.deepStart(Stream.of(POSTGRES_CONTAINER, REDIS_CONTAINER, KAFKA_CONTAINER))
                .join();
        
        // Validate container health
        validateContainerHealth();
    }

    /**
     * Stop all containers after all tests complete.
     */
    @AfterAll
    static void stopContainers() {
        // Containers will be stopped automatically by Testcontainers
        // if reuse is disabled, otherwise they'll remain running
    }

    /**
     * Configure Spring application properties dynamically based on container ports.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Database configuration
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        
        // JPA/Hibernate configuration for tests
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "false");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        
        // Redis configuration
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
        registry.add("spring.data.redis.timeout", () -> "2000ms");
        registry.add("spring.data.redis.lettuce.pool.max-active", () -> "8");
        registry.add("spring.data.redis.lettuce.pool.max-idle", () -> "8");
        registry.add("spring.data.redis.lettuce.pool.min-idle", () -> "0");
        
        // Kafka configuration
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "user-service-test-group");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.key-deserializer", () -> "org.apache.kafka.common.serialization.StringDeserializer");
        registry.add("spring.kafka.consumer.value-deserializer", () -> "org.springframework.kafka.support.serializer.JsonDeserializer");
        registry.add("spring.kafka.producer.key-serializer", () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer", () -> "org.springframework.kafka.support.serializer.JsonSerializer");
        
        // Application-specific test configurations
        registry.add("app.security.jwt.enabled", () -> "false");
        registry.add("app.cache.enabled", () -> "true");
        registry.add("app.events.enabled", () -> "true");
        registry.add("app.file-upload.enabled", () -> "false");
        
        // Logging configuration for tests
        registry.add("logging.level.com.doordash.user_service", () -> "DEBUG");
        registry.add("logging.level.org.springframework.web", () -> "INFO");
        registry.add("logging.level.org.springframework.security", () -> "INFO");
        registry.add("logging.level.org.hibernate.SQL", () -> "WARN");
        registry.add("logging.level.org.testcontainers", () -> "WARN");
        
        // Performance optimizations for tests
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "5");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "1");
        registry.add("spring.datasource.hikari.connection-timeout", () -> "10000");
        registry.add("spring.datasource.hikari.idle-timeout", () -> "300000");
    }

    /**
     * Get the base URL for the running application.
     */
    protected String getBaseUrl() {
        return "http://localhost:" + port;
    }

    /**
     * Get the API base URL.
     */
    protected String getApiBaseUrl() {
        return getBaseUrl() + "/api/v1";
    }

    /**
     * Get the users API URL.
     */
    protected String getUsersApiUrl() {
        return getApiBaseUrl() + "/users";
    }

    /**
     * Validate that all containers are healthy and ready.
     */
    private static void validateContainerHealth() {
        if (!POSTGRES_CONTAINER.isRunning()) {
            throw new RuntimeException("PostgreSQL container failed to start");
        }
        
        if (!REDIS_CONTAINER.isRunning()) {
            throw new RuntimeException("Redis container failed to start");
        }
        
        if (!KAFKA_CONTAINER.isRunning()) {
            throw new RuntimeException("Kafka container failed to start");
        }
        
        // Additional health checks can be added here
        System.out.println("All test containers are healthy and ready");
        System.out.println("PostgreSQL: " + POSTGRES_CONTAINER.getJdbcUrl());
        System.out.println("Redis: " + REDIS_CONTAINER.getHost() + ":" + REDIS_CONTAINER.getMappedPort(6379));
        System.out.println("Kafka: " + KAFKA_CONTAINER.getBootstrapServers());
    }

    /**
     * Helper method to wait for application to be ready.
     */
    protected void waitForApplicationReady() {
        // Implementation can include health check endpoint polling
        try {
            Thread.sleep(1000); // Basic wait - can be enhanced with actual health checks
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for application", e);
        }
    }

    /**
     * Clean up method to be called between tests if needed.
     */
    protected void cleanupTestData() {
        // This method can be overridden by subclasses to perform
        // specific cleanup operations between tests
    }

    /**
     * Get container information for debugging.
     */
    protected String getContainerInfo() {
        return String.format(
            "Container Info - PostgreSQL: %s, Redis: %s:%d, Kafka: %s",
            POSTGRES_CONTAINER.getJdbcUrl(),
            REDIS_CONTAINER.getHost(),
            REDIS_CONTAINER.getMappedPort(6379),
            KAFKA_CONTAINER.getBootstrapServers()
        );
    }
}
