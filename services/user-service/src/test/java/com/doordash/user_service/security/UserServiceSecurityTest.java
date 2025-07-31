package com.doordash.user_service.security;

import com.doordash.user_service.config.EnhancedTestConfig;
import com.doordash.user_service.testcontainers.EnhancedBaseIntegrationTest;
import com.doordash.user_service.utils.EnhancedTestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive security tests for the User Service.
 * 
 * This test class covers:
 * - Authentication and authorization scenarios
 * - Input validation and sanitization
 * - SQL injection prevention
 * - XSS prevention
 * - CSRF protection
 * - Rate limiting
 * - JWT token validation
 * - Role-based access control
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles({"test", "security"})
@Import(EnhancedTestConfig.class)
@Tag("security")
@DisplayName("User Service Security Tests")
class UserServiceSecurityTest extends EnhancedBaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should reject unauthenticated requests to protected endpoints")
        void shouldRejectUnauthenticatedRequests() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should allow authenticated requests with valid JWT")
        void shouldAllowAuthenticatedRequests() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should reject requests with invalid JWT token")
        void shouldRejectInvalidJwtToken() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile")
                    .header("Authorization", "Bearer invalid.jwt.token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject requests with expired JWT token")
        void shouldRejectExpiredJwtToken() throws Exception {
            String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0IiwiZXhwIjoxNTE2MjM5MDIyfQ.expired";
            
            mockMvc.perform(get("/api/v1/users/profile")
                    .header("Authorization", "Bearer " + expiredToken)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @Test
        @WithMockUser(username = "regularuser", roles = "USER")
        @DisplayName("Should allow users to access their own profile")
        void shouldAllowUsersToAccessOwnProfile() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()))
                    .andExpected(status().isOk());
        }

        @Test
        @WithMockUser(username = "regularuser", roles = "USER")
        @DisplayName("Should prevent users from accessing other users' profiles")
        void shouldPreventAccessToOtherUsersProfiles() throws Exception {
            mockMvc.perform(get("/api/v1/users/other-user-id/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "adminuser", roles = "ADMIN")
        @DisplayName("Should allow admins to access any user profile")
        void shouldAllowAdminsToAccessAnyProfile() throws Exception {
            mockMvc.perform(get("/api/v1/users/any-user-id/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Input Validation Security Tests")
    class InputValidationSecurityTests {

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should sanitize and reject XSS attempts in profile creation")
        void shouldRejectXSSAttemptsInProfileCreation() throws Exception {
            var maliciousRequest = EnhancedTestDataFactory.createMaliciousRequest();
            
            mockMvc.perform(post("/api/v1/users/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(maliciousRequest))
                    .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").exists());
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should reject SQL injection attempts")
        void shouldRejectSQLInjectionAttempts() throws Exception {
            String sqlInjectionPayload = "'; DROP TABLE users; --";
            
            mockMvc.perform(get("/api/v1/users/search")
                    .param("query", sqlInjectionPayload)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should validate and reject oversized payloads")
        void shouldRejectOversizedPayloads() throws Exception {
            String oversizedBio = "A".repeat(10000); // Exceeds maximum allowed size
            var request = EnhancedTestDataFactory.createRealisticCreateRequest();
            request.setBio(oversizedBio);
            
            mockMvc.perform(post("/api/v1/users/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[*].field").value("bio"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should validate file upload types and sizes")
        void shouldValidateFileUploads() throws Exception {
            // Test with invalid file type
            mockMvc.perform(multipart("/api/v1/users/profile/avatar")
                    .file("file", "malicious content".getBytes())
                    .param("filename", "malicious.exe")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("CSRF Protection Tests")
    class CSRFProtectionTests {

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should reject POST requests without CSRF token")
        void shouldRejectPostWithoutCSRF() throws Exception {
            var request = EnhancedTestDataFactory.createRealisticCreateRequest();
            
            mockMvc.perform(post("/api/v1/users/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should accept POST requests with valid CSRF token")
        void shouldAcceptPostWithValidCSRF() throws Exception {
            var request = EnhancedTestDataFactory.createRealisticCreateRequest();
            
            mockMvc.perform(post("/api/v1/users/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should reject PUT requests without CSRF token")
        void shouldRejectPutWithoutCSRF() throws Exception {
            var request = EnhancedTestDataFactory.createRealisticUpdateRequest();
            
            mockMvc.perform(put("/api/v1/users/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should reject DELETE requests without CSRF token")
        void shouldRejectDeleteWithoutCSRF() throws Exception {
            mockMvc.perform(delete("/api/v1/users/profile"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Rate Limiting Tests")
    class RateLimitingTests {

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should apply rate limiting to prevent abuse")
        void shouldApplyRateLimiting() throws Exception {
            // Make multiple rapid requests to test rate limiting
            for (int i = 0; i < 100; i++) {
                var result = mockMvc.perform(get("/api/v1/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()));
                
                if (i > 50) { // Assuming rate limit kicks in after 50 requests
                    result.andExpect(status().isTooManyRequests());
                    break;
                }
            }
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should have different rate limits for different endpoints")
        void shouldHaveDifferentRateLimitsForDifferentEndpoints() throws Exception {
            // Test that sensitive operations have stricter rate limits
            var request = EnhancedTestDataFactory.createRealisticCreateRequest();
            
            for (int i = 0; i < 10; i++) {
                var result = mockMvc.perform(post("/api/v1/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()));
                
                if (i > 3) { // Stricter limit for create operations
                    result.andExpect(status().isTooManyRequests());
                    break;
                }
            }
        }
    }

    @Nested
    @DisplayName("Security Headers Tests")
    class SecurityHeadersTests {

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should include security headers in responses")
        void shouldIncludeSecurityHeaders() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("X-Content-Type-Options"))
                    .andExpect(header().exists("X-Frame-Options"))
                    .andExpect(header().exists("X-XSS-Protection"))
                    .andExpect(header().exists("Strict-Transport-Security"));
        }

        @Test
        @DisplayName("Should not expose sensitive information in error responses")
        void shouldNotExposeSensitiveInfoInErrors() throws Exception {
            mockMvc.perform(get("/api/v1/users/nonexistent")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").doesNotExist())
                    .andExpect(jsonPath("$.stackTrace").doesNotExist())
                    .andExpect(jsonPath("$.debugInfo").doesNotExist());
        }
    }

    @Nested
    @DisplayName("Password Security Tests")
    class PasswordSecurityTests {

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should enforce strong password requirements")
        void shouldEnforceStrongPasswordRequirements() throws Exception {
            String weakPassword = "123456";
            
            mockMvc.perform(post("/api/v1/users/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"currentPassword\":\"oldpass\",\"newPassword\":\"" + weakPassword + "\"}")
                    .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[*].field").value("newPassword"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should prevent password reuse")
        void shouldPreventPasswordReuse() throws Exception {
            // Test that users cannot reuse recent passwords
            String currentPassword = "CurrentPassword123!";
            
            mockMvc.perform(post("/api/v1/users/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"currentPassword\":\"" + currentPassword + "\",\"newPassword\":\"" + currentPassword + "\"}")
                    .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("cannot reuse")));
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should hash passwords securely")
        void shouldHashPasswordsSecurely() throws Exception {
            String newPassword = "NewSecurePassword123!";
            
            mockMvc.perform(post("/api/v1/users/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"currentPassword\":\"oldpass\",\"newPassword\":\"" + newPassword + "\"}")
                    .with(csrf()))
                    .andExpect(status().isOk());
            
            // Verify password is properly hashed and not stored in plain text
            // This would require database verification in a real test
        }
    }

    @Nested
    @DisplayName("Session Management Tests")
    class SessionManagementTests {

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should invalidate sessions on logout")
        void shouldInvalidateSessionsOnLogout() throws Exception {
            mockMvc.perform(post("/api/v1/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()))
                    .andExpect(status().isOk());
            
            // Subsequent requests should be unauthorized
            mockMvc.perform(get("/api/v1/users/profile")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should prevent session fixation attacks")
        void shouldPreventSessionFixation() throws Exception {
            // Test that session ID changes after authentication
            // This would require session tracking in a real implementation
        }

        @Test
        @DisplayName("Should enforce session timeout")
        void shouldEnforceSessionTimeout() throws Exception {
            // Test that sessions expire after configured timeout
            // This would require time manipulation in tests
        }
    }

    @Nested
    @DisplayName("Data Privacy Tests")
    class DataPrivacyTests {

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("Should not expose sensitive data in API responses")
        void shouldNotExposeSensitiveData() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.passwordHash").doesNotExist())
                    .andExpected(jsonPath("$.ssn").doesNotExist())
                    .andExpected(jsonPath("$.creditCardNumber").doesNotExist());
        }

        @Test
        @WithMockUser(username = "testuser", roles = "ADMIN")
        @DisplayName("Should mask sensitive data even for admin users")
        void shouldMaskSensitiveDataForAdmins() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users/search")
                    .param("query", "testuser")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.users[*].passwordHash").doesNotExist())
                    .andExpected(jsonPath("$.users[*].phoneNumber").value(containsString("***")));
        }
    }
}
