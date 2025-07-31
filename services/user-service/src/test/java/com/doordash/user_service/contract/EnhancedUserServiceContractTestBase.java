package com.doordash.user_service.contract;

import com.doordash.user_service.config.EnhancedTestConfig;
import com.doordash.user_service.domain.entities.User;
import com.doordash.user_service.domain.entities.UserProfile;
import com.doordash.user_service.repositories.UserRepository;
import com.doordash.user_service.utils.EnhancedTestDataFactory;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Enhanced base class for contract testing.
 * 
 * This class provides the foundation for Spring Cloud Contract tests,
 * ensuring API compatibility between services in the microservices architecture.
 * 
 * Contract tests verify:
 * - API request/response structures
 * - Data type compatibility
 * - HTTP status codes
 * - Header requirements
 * - Error response formats
 * - Backward compatibility
 * 
 * @author DoorDash Backend Team
 * @version 2.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebMvc
@AutoConfigureMessageVerifier
@DirtiesContext
@Transactional
@ActiveProfiles({"test", "contract"})
@Import(EnhancedTestConfig.class)
@Tag("contract")
public abstract class EnhancedUserServiceContractTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private UserProfile testUserProfile;

    @BeforeEach
    public void setup() {
        // Configure RestAssured to use MockMvc
        RestAssuredMockMvc.mockMvc(mockMvc);
        
        // Setup test data for contract tests
        setupTestData();
    }

    /**
     * Setup consistent test data for contract verification.
     */
    private void setupTestData() {
        // Create a consistent test user for contract testing
        testUser = User.builder()
            .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
            .email("contract.test@doordash.com")
            .username("contractuser")
            .firstName("Contract")
            .lastName("TestUser")
            .phoneNumber("+1234567890")
            .passwordHash("$2a$10$contract.test.hash")
            .status(com.doordash.user_service.domain.enums.UserStatus.ACTIVE)
            .emailVerified(true)
            .createdAt(java.time.LocalDateTime.of(2024, 1, 1, 12, 0, 0))
            .updatedAt(java.time.LocalDateTime.of(2024, 1, 1, 12, 0, 0))
            .build();

        testUserProfile = UserProfile.builder()
            .id(UUID.fromString("660e8400-e29b-41d4-a716-446655440001"))
            .user(testUser)
            .firstName("Contract")
            .lastName("TestUser")
            .dateOfBirth(java.time.LocalDate.of(1990, 1, 1))
            .phoneNumber("+1234567890")
            .profilePictureUrl("https://example.com/profile.jpg")
            .bio("Contract testing user profile")
            .city("San Francisco")
            .state("CA")
            .zipCode("94103")
            .createdAt(java.time.LocalDateTime.of(2024, 1, 1, 12, 0, 0))
            .updatedAt(java.time.LocalDateTime.of(2024, 1, 1, 12, 0, 0))
            .build();

        testUser.setProfile(testUserProfile);
        userRepository.save(testUser);
    }

    // Helper methods for contract tests

    /**
     * Get the test user ID for contract tests.
     */
    protected String getTestUserId() {
        return testUser.getId().toString();
    }

    /**
     * Get the test user email for contract tests.
     */
    protected String getTestUserEmail() {
        return testUser.getEmail();
    }

    /**
     * Get the test user profile ID for contract tests.
     */
    protected String getTestUserProfileId() {
        return testUserProfile.getId().toString();
    }

    /**
     * Create a valid JWT token for the test user.
     */
    protected String createValidJwtToken() {
        return "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.contract.token";
    }

    /**
     * Create an invalid JWT token for negative testing.
     */
    protected String createInvalidJwtToken() {
        return "Bearer invalid.jwt.token";
    }

    /**
     * Create a valid create profile request for contract testing.
     */
    protected String createValidProfileRequest() {
        return """
            {
                "firstName": "John",
                "lastName": "Doe",
                "dateOfBirth": "1990-01-01",
                "phoneNumber": "+1234567890",
                "bio": "Contract test bio",
                "city": "San Francisco",
                "state": "CA",
                "zipCode": "94103"
            }
            """;
    }

    /**
     * Create an invalid create profile request for contract testing.
     */
    protected String createInvalidProfileRequest() {
        return """
            {
                "firstName": "",
                "lastName": null,
                "dateOfBirth": "2030-01-01",
                "phoneNumber": "123",
                "bio": null,
                "city": null,
                "state": "INVALID",
                "zipCode": "12"
            }
            """;
    }

    /**
     * Create a valid update profile request for contract testing.
     */
    protected String createValidUpdateRequest() {
        return """
            {
                "firstName": "Jane",
                "lastName": "Smith",
                "phoneNumber": "+0987654321",
                "bio": "Updated contract test bio",
                "city": "Los Angeles",
                "state": "CA",
                "zipCode": "90210"
            }
            """;
    }

    /**
     * Create expected profile response for contract verification.
     */
    protected String getExpectedProfileResponse() {
        return """
            {
                "id": "660e8400-e29b-41d4-a716-446655440001",
                "userId": "550e8400-e29b-41d4-a716-446655440000",
                "firstName": "Contract",
                "lastName": "TestUser",
                "email": "contract.test@doordash.com",
                "dateOfBirth": "1990-01-01",
                "phoneNumber": "+1234567890",
                "profilePictureUrl": "https://example.com/profile.jpg",
                "bio": "Contract testing user profile",
                "city": "San Francisco",
                "state": "CA",
                "zipCode": "94103",
                "createdAt": "2024-01-01T12:00:00",
                "updatedAt": "2024-01-01T12:00:00"
            }
            """;
    }

    /**
     * Create expected error response for contract verification.
     */
    protected String getExpectedErrorResponse() {
        return """
            {
                "timestamp": "2024-01-01T12:00:00Z",
                "status": 400,
                "error": "Bad Request",
                "message": "Validation failed",
                "path": "/api/v1/users/profile",
                "errors": [
                    {
                        "field": "firstName",
                        "rejectedValue": "",
                        "message": "First name is required"
                    }
                ]
            }
            """;
    }

    /**
     * Create expected unauthorized response for contract verification.
     */
    protected String getExpectedUnauthorizedResponse() {
        return """
            {
                "timestamp": "2024-01-01T12:00:00Z",
                "status": 401,
                "error": "Unauthorized",
                "message": "Authentication required",
                "path": "/api/v1/users/profile"
            }
            """;
    }

    /**
     * Create expected not found response for contract verification.
     */
    protected String getExpectedNotFoundResponse() {
        return """
            {
                "timestamp": "2024-01-01T12:00:00Z",
                "status": 404,
                "error": "Not Found",
                "message": "User profile not found",
                "path": "/api/v1/users/profile"
            }
            """;
    }

    /**
     * Clean up test data after contract tests.
     */
    protected void cleanupContractTestData() {
        userRepository.deleteAll();
    }
}
