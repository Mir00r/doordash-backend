package com.doordash.user_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced test configuration providing comprehensive testing infrastructure.
 * 
 * This configuration sets up:
 * - Mock security context with JWT authentication
 * - Test-specific beans and configurations
 * - Performance testing optimizations
 * - Security testing configurations
 * - Database and messaging test infrastructure
 * 
 * @author DoorDash Backend Team
 * @version 2.0
 */
@TestConfiguration
@ActiveProfiles("test")
public class EnhancedTestConfig {

    /**
     * Custom ObjectMapper for test scenarios with proper time handling.
     */
    @Bean
    @Primary
    public ObjectMapper testObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();
        return mapper;
    }

    /**
     * Password encoder for test scenarios.
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        // Use weaker settings for faster test execution
        return new BCryptPasswordEncoder(4);
    }

    /**
     * Mock JWT decoder for security testing.
     */
    @Bean
    @Primary
    @Profile("test")
    public JwtDecoder mockJwtDecoder() {
        return new MockJwtDecoder();
    }

    /**
     * Redis template configuration for caching tests.
     */
    @Bean
    @Primary
    @Profile("test")
    public RedisTemplate<String, Object> testRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Kafka template for messaging tests.
     */
    @Bean
    @Primary
    @Profile("test")
    public KafkaTemplate<String, Object> testKafkaTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.springframework.kafka.support.serializer.JsonSerializer");
        
        ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * Mock JWT decoder implementation for testing.
     */
    private static class MockJwtDecoder implements JwtDecoder {
        
        @Override
        public Jwt decode(String token) throws RuntimeException {
            Map<String, Object> headers = Map.of(
                "alg", "RS256",
                "typ", "JWT"
            );
            
            Map<String, Object> claims = Map.of(
                "sub", "test-user-123",
                "preferred_username", "testuser",
                "email", "test@doordash.com",
                "given_name", "Test",
                "family_name", "User",
                "roles", "USER",
                "exp", Instant.now().plusSeconds(3600),
                "iat", Instant.now()
            );
            
            return new Jwt(
                token,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                headers,
                claims
            );
        }
    }

    /**
     * Test containers configuration for different testing scenarios.
     */
    @TestConfiguration
    @Profile("integration")
    public static class IntegrationTestConfig {
        
        @Bean
        public PostgreSQLContainer<?> postgresContainer() {
            PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("user_service_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
            container.start();
            return container;
        }

        @Bean
        public GenericContainer<?> redisContainer() {
            GenericContainer<?> container = new GenericContainer<>("redis:7-alpine")
                .withExposedPorts(6379)
                .withReuse(true);
            container.start();
            return container;
        }

        @Bean
        public KafkaContainer kafkaContainer() {
            KafkaContainer container = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
                .withReuse(true);
            container.start();
            return container;
        }
    }

    /**
     * Performance test configuration with optimized settings.
     */
    @TestConfiguration
    @Profile("performance")
    public static class PerformanceTestConfig {
        
        @Bean
        @Primary
        public RedisTemplate<String, Object> performanceRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(redisConnectionFactory);
            // Use faster serializers for performance tests
            template.setDefaultSerializer(new StringRedisSerializer());
            template.afterPropertiesSet();
            return template;
        }
    }

    /**
     * Security test configuration with enhanced security context.
     */
    @TestConfiguration
    @Profile("security")
    public static class SecurityTestConfig {
        
        @Bean
        @Primary
        public PasswordEncoder securityTestPasswordEncoder() {
            // Use stronger settings for security tests
            return new BCryptPasswordEncoder(12);
        }
    }

    /**
     * Contract test configuration for API compatibility testing.
     */
    @TestConfiguration
    @Profile("contract")
    public static class ContractTestConfig {
        
        @Bean
        @Primary
        public ObjectMapper contractObjectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            // Strict serialization for contract compliance
            mapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, false);
            return mapper;
        }
    }

    /**
     * E2E test configuration with full application context.
     */
    @TestConfiguration
    @Profile("e2e")
    public static class E2ETestConfig {
        
        // E2E specific configurations can be added here
        // This might include external service mocks, etc.
    }
}
