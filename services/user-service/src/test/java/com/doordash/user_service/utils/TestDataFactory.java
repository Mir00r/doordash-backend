package com.doordash.user_service.utils;

import com.doordash.user_service.domain.dtos.user.CreateUserProfileRequest;
import com.doordash.user_service.domain.dtos.user.UpdateUserProfileRequest;
import com.doordash.user_service.domain.dtos.user.UserProfileResponse;
import com.doordash.user_service.domain.entities.User;
import com.doordash.user_service.domain.entities.UserProfile;
import com.doordash.user_service.domain.enums.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.datafaker.Faker;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Enhanced utility class for comprehensive test data creation and common test operations.
 * 
 * Provides factory methods for creating test entities, DTOs, and mock objects with
 * realistic data using Faker library for better test scenarios.
 * Centralizes test data creation to ensure consistency across test classes.
 * 
 * Features:
 * - Realistic test data generation
 * - Builder patterns for complex objects
 * - Performance testing data sets
 * - Edge case data scenarios
 * - JSON serialization/deserialization helpers
 * 
 * @author DoorDash Backend Team
 * @version 2.0
 */
public final class TestDataFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    
    private static final Faker faker = new Faker();
    private static final Random random = new Random();

    // Predefined test data sets
    private static final List<String> TEST_CITIES = Arrays.asList(
        "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", 
        "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose"
    );
    
    private static final List<String> TEST_STATES = Arrays.asList(
        "NY", "CA", "IL", "TX", "AZ", "PA", "TX", "CA", "TX", "CA"
    );

    private TestDataFactory() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a test User entity with realistic fake data.
     */
    public static User createTestUser() {
        return createTestUser(UUID.randomUUID());
    }

    /**
     * Creates a test User entity with specified ID and realistic fake data.
     */
    public static User createTestUser(UUID id) {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String email = faker.internet().emailAddress(firstName.toLowerCase() + "." + lastName.toLowerCase());
        String username = faker.internet().username();
        
        return User.builder()
            .id(id)
            .email(email)
            .username(username)
            .firstName(firstName)
            .lastName(lastName)
            .phoneNumber(faker.phoneNumber().phoneNumber())
            .status(UserStatus.ACTIVE)
            .createdAt(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 365)))
            .updatedAt(LocalDateTime.now())
            .build();
    }

    /**
     * Creates a test User entity with specified values.
     */
    public static User createTestUser(UUID id, String email, String username) {
        return User.builder()
            .id(id)
            .email(email)
            .username(username)
            .passwordHash("$2a$10$hashed.password.example")
            .status(UserStatus.ACTIVE)
            .emailVerified(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    /**
     * Creates a test User with UserProfile.
     */
    public static User createTestUserWithProfile() {
        User user = createTestUser();
        UserProfile profile = createTestUserProfile(user);
        user.setProfile(profile);
        return user;
    }

    /**
     * Creates a test UserProfile entity.
     */
    public static UserProfile createTestUserProfile(User user) {
        return UserProfile.builder()
            .id(UUID.randomUUID())
            .user(user)
            .firstName("John")
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .phoneNumber("+1234567890")
            .profilePictureUrl("/images/profiles/default.jpg")
            .bio("Test user biography")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    /**
     * Creates a test CreateUserProfileRequest DTO.
     */
    public static CreateUserProfileRequest createTestCreateUserProfileRequest() {
        return CreateUserProfileRequest.builder()
            .firstName("John")
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .phoneNumber("+1234567890")
            .bio("Test user biography")
            .build();
    }

    /**
     * Creates a test CreateUserProfileRequest with custom values.
     */
    public static CreateUserProfileRequest createTestCreateUserProfileRequest(
            String firstName, String lastName, String phoneNumber) {
        return CreateUserProfileRequest.builder()
            .firstName(firstName)
            .lastName(lastName)
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .phoneNumber(phoneNumber)
            .bio("Test user biography")
            .build();
    }

    /**
     * Creates a test UpdateUserProfileRequest DTO.
     */
    public static UpdateUserProfileRequest createTestUpdateUserProfileRequest() {
        return UpdateUserProfileRequest.builder()
            .firstName("Jane")
            .lastName("Smith")
            .phoneNumber("+0987654321")
            .bio("Updated test user biography")
            .build();
    }

    /**
     * Creates an invalid CreateUserProfileRequest for validation testing.
     */
    public static CreateUserProfileRequest createInvalidCreateUserProfileRequest() {
        return CreateUserProfileRequest.builder()
            .firstName("") // Invalid - empty
            .lastName(null) // Invalid - null
            .dateOfBirth(LocalDate.now().plusDays(1)) // Invalid - future date
            .phoneNumber("123") // Invalid - too short
            .bio("A".repeat(2000)) // Invalid - too long
            .build();
    }

    /**
     * Creates HTTP headers with authentication for testing.
     */
    public static HttpHeaders createAuthenticatedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + createTestJwtToken());
        return headers;
    }

    /**
     * Creates HTTP headers without authentication.
     */
    public static HttpHeaders createUnauthenticatedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Creates a test JWT token for authentication testing.
     */
    public static String createTestJwtToken() {
        return createTestJwtToken("test-user-123", "testuser", "test@doordash.com");
    }

    /**
     * Creates a test JWT token with custom claims.
     */
    public static String createTestJwtToken(String userId, String username, String email) {
        // In a real scenario, this would be a properly signed JWT
        // For testing, we return a mock token
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    }

    /**
     * Creates a mock Jwt object for Spring Security testing.
     */
    public static Jwt createMockJwt() {
        return createMockJwt("test-user-123", "testuser", "test@doordash.com");
    }

    /**
     * Creates a mock Jwt object with custom claims.
     */
    public static Jwt createMockJwt(String userId, String username, String email) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        headers.put("typ", "JWT");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("preferred_username", username);
        claims.put("email", email);
        claims.put("given_name", "Test");
        claims.put("family_name", "User");
        claims.put("roles", "USER");
        claims.put("exp", Instant.now().plusSeconds(3600));
        claims.put("iat", Instant.now());

        return new Jwt(
            "test-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            headers,
            claims
        );
    }

    /**
     * Creates test data for bulk operations.
     */
    public static User[] createTestUsers(int count) {
        User[] users = new User[count];
        for (int i = 0; i < count; i++) {
            users[i] = createTestUser(
                UUID.randomUUID(),
                "test" + i + "@doordash.com",
                "testuser" + i
            );
        }
        return users;
    }

    /**
     * Creates test data for pagination testing.
     */
    public static User[] createTestUsersForPagination() {
        return createTestUsers(25); // Default page size + 5 for pagination testing
    }

    /**
     * Converts object to JSON string for HTTP requests.
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * Converts JSON string to object for HTTP responses.
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }

    /**
     * Extracts JSON response from MvcResult.
     */
    public static String extractJsonResponse(MvcResult result) {
        try {
            return result.getResponse().getContentAsString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract JSON response", e);
        }
    }

    /**
     * Creates test data for performance testing.
     */
    public static class PerformanceTestData {
        
        public static CreateUserProfileRequest[] createBulkCreateRequests(int count) {
            CreateUserProfileRequest[] requests = new CreateUserProfileRequest[count];
            for (int i = 0; i < count; i++) {
                requests[i] = CreateUserProfileRequest.builder()
                    .firstName("PerfUser" + i)
                    .lastName("Test" + i)
                    .dateOfBirth(LocalDate.of(1990, 1, (i % 28) + 1))
                    .phoneNumber("+123456789" + (i % 10))
                    .bio("Performance test user " + i)
                    .build();
            }
            return requests;
        }

        public static User[] createBulkUsers(int count) {
            User[] users = new User[count];
            for (int i = 0; i < count; i++) {
                users[i] = User.builder()
                    .id(UUID.randomUUID())
                    .email("perftest" + i + "@doordash.com")
                    .username("perfuser" + i)
                    .passwordHash("$2a$10$hashed.password.example")
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            }
            return users;
        }
    }

    /**
     * Creates test data for security testing.
     */
    public static class SecurityTestData {
        
        public static CreateUserProfileRequest createMaliciousRequest() {
            return CreateUserProfileRequest.builder()
                .firstName("<script>alert('XSS')</script>")
                .lastName("'; DROP TABLE users; --")
                .phoneNumber("+1234567890")
                .bio("Malicious content with <img src='x' onerror='alert(1)'>")
                .build();
        }

        public static String createInvalidJwtToken() {
            return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature";
        }

        public static String createExpiredJwtToken() {
            // Mock expired token
            return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired.token";
        }
    }

    /**
     * Creates test data for edge cases.
     */
    public static class EdgeCaseTestData {
        
        public static CreateUserProfileRequest createBoundaryValueRequest() {
            return CreateUserProfileRequest.builder()
                .firstName("A") // Minimum length
                .lastName("B".repeat(50)) // Maximum length
                .dateOfBirth(LocalDate.of(1900, 1, 1)) // Very old date
                .phoneNumber("+1234567890123456") // Long phone number
                .bio("") // Empty bio
                .build();
        }

        public static CreateUserProfileRequest createUnicodeRequest() {
            return CreateUserProfileRequest.builder()
                .firstName("José")
                .lastName("González-Müller")
                .phoneNumber("+49123456789")
                .bio("Bio with unicode: 你好世界 🌍 émojis")
                .build();
        }

        public static User createUserWithSpecialCharacters() {
            return User.builder()
                .id(UUID.randomUUID())
                .email("test+tag@example.co.uk")
                .username("user.name-123")
                .passwordHash("$2a$10$hashed.password.example")
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        }
    }
}
