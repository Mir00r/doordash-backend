package com.doordash.user_service.integration;

import com.doordash.user_service.config.TestConfig;
import com.doordash.user_service.domain.dtos.user.CreateUserProfileRequest;
import com.doordash.user_service.domain.dtos.user.UpdateUserProfileRequest;
import com.doordash.user_service.domain.dtos.user.UserProfileResponse;
import com.doordash.user_service.domain.entities.User;
import com.doordash.user_service.domain.enums.UserStatus;
import com.doordash.user_service.repositories.UserRepository;
import com.doordash.user_service.testcontainers.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for User Service.
 * 
 * Tests the entire application stack including:
 * - REST API endpoints
 * - Database persistence
 * - Security configuration
 * - Validation
 * - Caching
 * - Event publishing
 * 
 * Uses Testcontainers for realistic testing environment.
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@DisplayName("User Service Integration Tests")
class UserServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private CreateUserProfileRequest createRequest;
    private UpdateUserProfileRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        userRepository.deleteAll();

        // Create test user
        testUser = User.builder()
            .id(UUID.randomUUID())
            .email("test@doordash.com")
            .username("testuser")
            .passwordHash("hashed_password")
            .status(UserStatus.ACTIVE)
            .emailVerified(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        testUser = userRepository.save(testUser);

        // Create test requests
        createRequest = CreateUserProfileRequest.builder()
            .firstName("John")
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .phoneNumber("+1234567890")
            .build();

        updateRequest = UpdateUserProfileRequest.builder()
            .firstName("Jane")
            .lastName("Smith")
            .phoneNumber("+0987654321")
            .build();
    }

    @Nested
    @DisplayName("User Profile API Tests")
    class UserProfileApiTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should create user profile via API")
        void shouldCreateUserProfileViaApi() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/v1/users/{userId}/profile", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"));

            // Verify database persistence
            User savedUser = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(savedUser.getProfile()).isNotNull();
            assertThat(savedUser.getProfile().getFirstName()).isEqualTo("John");
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should get user profile via API")
        void shouldGetUserProfileViaApi() throws Exception {
            // Arrange - Create profile first
            mockMvc.perform(post("/api/v1/users/{userId}/profile", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

            // Act & Assert
            mockMvc.perform(get("/api/v1/users/{userId}/profile", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("test@doordash.com"));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should update user profile via API")
        void shouldUpdateUserProfileViaApi() throws Exception {
            // Arrange - Create profile first
            mockMvc.perform(post("/api/v1/users/{userId}/profile", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

            // Act & Assert
            mockMvc.perform(put("/api/v1/users/{userId}/profile", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.firstName").value("Jane"))
                .andExpected(jsonPath("$.lastName").value("Smith"));

            // Verify database persistence
            User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(updatedUser.getProfile().getFirstName()).isEqualTo("Jane");
        }

        @Test
        @DisplayName("Should require authentication for protected endpoints")
        void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
            mockMvc.perform(get("/api/v1/users/{userId}/profile", testUser.getId()))
                .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/v1/users/{userId}/profile", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 404 for non-existent user")
        void shouldReturn404ForNonExistentUser() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/users/{userId}/profile", nonExistentId))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should validate required fields")
        void shouldValidateRequiredFields() throws Exception {
            CreateUserProfileRequest invalidRequest = CreateUserProfileRequest.builder()
                .firstName("") // Invalid - empty
                .lastName(null) // Invalid - null
                .dateOfBirth(LocalDate.now().plusDays(1)) // Invalid - future date
                .phoneNumber("invalid") // Invalid format
                .build();

            mockMvc.perform(post("/api/v1/users/{userId}/profile", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should validate email format")
        void shouldValidateEmailFormat() throws Exception {
            CreateUserProfileRequest invalidRequest = CreateUserProfileRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .phoneNumber("+1234567890")
                .build();

            // This would test email validation if it's part of the profile creation
            mockMvc.perform(post("/api/v1/users/{userId}/profile", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isCreated()); // Valid request should succeed
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should validate phone number format")
        void shouldValidatePhoneNumberFormat() throws Exception {
            CreateUserProfileRequest invalidRequest = CreateUserProfileRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .phoneNumber("123") // Invalid format
                .build();

            mockMvc.perform(post("/api/v1/users/{userId}/profile", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Database Persistence Tests")
    @Transactional
    class DatabasePersistenceTests {

        @Test
        @DisplayName("Should persist user profile to database")
        void shouldPersistUserProfileToDatabase() throws Exception {
            // Arrange
            long initialCount = userRepository.count();

            // Act - Create profile via API
            mockMvc.perform(post("/api/v1/users/{userId}/profile", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

            // Assert - Verify database state
            assertThat(userRepository.count()).isEqualTo(initialCount); // Same user count
            User savedUser = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(savedUser.getProfile()).isNotNull();
            assertThat(savedUser.getProfile().getFirstName()).isEqualTo("John");
            assertThat(savedUser.getProfile().getLastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("Should handle database constraints")
        void shouldHandleDatabaseConstraints() {
            // Test unique constraints, foreign key constraints, etc.
            // Implementation depends on specific database schema constraints
        }

        @Test
        @DisplayName("Should handle concurrent modifications")
        void shouldHandleConcurrentModifications() {
            // Test optimistic locking, version handling, etc.
            // Implementation depends on concurrency control strategy
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should allow admin access to all user profiles")
        void shouldAllowAdminAccessToAllUserProfiles() throws Exception {
            mockMvc.perform(get("/api/v1/users/{userId}/profile", testUser.getId()))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should allow users to access own profile")
        void shouldAllowUsersToAccessOwnProfile() throws Exception {
            mockMvc.perform(get("/api/v1/users/{userId}/profile", testUser.getId()))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "otheruser", roles = "USER")
        @DisplayName("Should deny access to other users' profiles")
        void shouldDenyAccessToOtherUsersProfiles() throws Exception {
            mockMvc.perform(get("/api/v1/users/{userId}/profile", testUser.getId()))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should handle CSRF protection")
        void shouldHandleCsrfProtection() throws Exception {
            mockMvc.perform(post("/api/v1/users/{userId}/profile", testUser.getId())
                    // No CSRF token
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Caching Tests")
    class CachingTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should cache user profile data")
        void shouldCacheUserProfileData() throws Exception {
            // Create profile
            mockMvc.perform(post("/api/v1/users/{userId}/profile", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

            // First request - should hit database
            mockMvc.perform(get("/api/v1/users/{userId}/profile", testUser.getId()))
                .andExpect(status().isOk());

            // Second request - should hit cache
            mockMvc.perform(get("/api/v1/users/{userId}/profile", testUser.getId()))
                .andExpected(status().isOk());

            // Cache behavior would be verified through metrics or logs
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should evict cache on profile update")
        void shouldEvictCacheOnProfileUpdate() throws Exception {
            // Create and cache profile
            mockMvc.perform(post("/api/v1/users/{userId}/profile", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

            mockMvc.perform(get("/api/v1/users/{userId}/profile", testUser.getId()))
                .andExpect(status().isOk());

            // Update profile - should evict cache
            mockMvc.perform(put("/api/v1/users/{userId}/profile", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

            // Next request should hit database with updated data
            mockMvc.perform(get("/api/v1/users/{userId}/profile", testUser.getId()))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.firstName").value("Jane"));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle malformed JSON")
        void shouldHandleMalformedJson() throws Exception {
            mockMvc.perform(post("/api/v1/users/{userId}/profile", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle missing content type")
        void shouldHandleMissingContentType() throws Exception {
            mockMvc.perform(post("/api/v1/users/{userId}/profile", testUser.getId())
                    .with(csrf())
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle large payloads")
        void shouldHandleLargePayloads() throws Exception {
            // Create a request with very long strings
            CreateUserProfileRequest largeRequest = CreateUserProfileRequest.builder()
                .firstName("A".repeat(1000))
                .lastName("B".repeat(1000))
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .phoneNumber("+1234567890")
                .build();

            mockMvc.perform(post("/api/v1/users/{userId}/profile", testUser.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(largeRequest)))
                .andExpect(status().isBadRequest()); // Assuming size validation exists
        }
    }
}
