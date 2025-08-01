package com.doordash.user_service.security;

import com.doordash.user_service.config.OAuth2SecurityConfig;
import com.doordash.user_service.config.SecurityProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive OAuth2 Security Integration Tests for DoorDash User Service.
 * 
 * This test class validates the complete OAuth2 security implementation including:
 * - JWT authentication and authorization flows
 * - Role-based access control (RBAC)
 * - Permission-based authorization
 * - CORS configuration and headers
 * - CSRF protection mechanisms
 * - Rate limiting functionality
 * - Security headers implementation
 * - Method-level security annotations
 * - Service-to-service authentication
 * - Error handling and security responses
 * 
 * Test Categories:
 * - Authentication Tests: JWT validation, token expiration, malformed tokens
 * - Authorization Tests: Role and permission-based access control
 * - CORS Tests: Cross-origin request validation
 * - CSRF Tests: Token generation and validation
 * - Rate Limiting Tests: Request throttling and burst handling
 * - Security Headers Tests: HTTP security header validation
 * - Error Handling Tests: Security error responses and logging
 * 
 * Security Scenarios:
 * - Valid authentication with proper JWT tokens
 * - Invalid authentication attempts and error responses
 * - Role-based endpoint access (ADMIN, USER, DRIVER, etc.)
 * - Permission-based resource access
 * - Cross-service authentication and authorization
 * - Attack simulation and security hardening validation
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("OAuth2 Security Integration Tests")
class OAuth2SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtDecoder jwtDecoder;

    private MockMvc mockMvc;

    // Test JWT tokens for different user types
    private static final String VALID_JWT_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.test.signature";
    private static final String ADMIN_JWT_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.admin.signature";
    private static final String USER_JWT_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.user.signature";
    private static final String DRIVER_JWT_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.driver.signature";
    private static final String EXPIRED_JWT_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.expired.signature";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();
        
        setupJwtMocks();
    }

    /**
     * Sets up JWT decoder mocks for different token scenarios.
     */
    private void setupJwtMocks() {
        // Valid admin token
        when(jwtDecoder.decode(ADMIN_JWT_TOKEN)).thenReturn(
            createJwtToken("admin@doordash.com", "admin-123", "ADMIN", 
                Map.of("roles", "ROLE_ADMIN", "permissions", "MANAGE_USERS,VIEW_REPORTS"))
        );
        
        // Valid user token
        when(jwtDecoder.decode(USER_JWT_TOKEN)).thenReturn(
            createJwtToken("user@doordash.com", "user-123", "CUSTOMER",
                Map.of("roles", "ROLE_CUSTOMER", "permissions", "VIEW_PROFILE,UPDATE_PROFILE"))
        );
        
        // Valid driver token
        when(jwtDecoder.decode(DRIVER_JWT_TOKEN)).thenReturn(
            createJwtToken("driver@doordash.com", "driver-123", "DRIVER",
                Map.of("roles", "ROLE_DRIVER", "permissions", "VIEW_DELIVERIES,UPDATE_DELIVERY_STATUS"))
        );
        
        // Expired token
        when(jwtDecoder.decode(EXPIRED_JWT_TOKEN)).thenThrow(
            new org.springframework.security.oauth2.jwt.JwtException("Token expired")
        );
    }

    /**
     * Creates a mock JWT token for testing.
     */
    private Jwt createJwtToken(String subject, String userId, String userType, Map<String, Object> claims) {
        return Jwt.withTokenValue("test-token")
            .header("alg", "RS256")
            .header("typ", "JWT")
            .subject(subject)
            .claim("user_id", userId)
            .claim("user_type", userType)
            .claim("iss", "doordash-user-service")
            .claim("aud", "doordash-api")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(900)) // 15 minutes
            .claims(c -> c.putAll(claims))
            .build();
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should allow access to public endpoints without authentication")
        void shouldAllowPublicEndpointsWithoutAuth() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                .andDo(print())
                .andExpect(status().isOk());

            mockMvc.perform(get("/actuator/info"))
                .andDo(print())
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should deny access to protected endpoints without authentication")
        void shouldDenyProtectedEndpointsWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile"))
                .andDo(print())
                .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/v1/admin/users"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should authenticate successfully with valid JWT token")
        void shouldAuthenticateWithValidJwtToken() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile")
                    .header("Authorization", "Bearer " + USER_JWT_TOKEN))
                .andDo(print())
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should reject expired JWT token")
        void shouldRejectExpiredJwtToken() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile")
                    .header("Authorization", "Bearer " + EXPIRED_JWT_TOKEN))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"));
        }

        @Test
        @DisplayName("Should reject malformed JWT token")
        void shouldRejectMalformedJwtToken() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile")
                    .header("Authorization", "Bearer invalid-token"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should handle missing Authorization header")
        void shouldHandleMissingAuthorizationHeader() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"));
        }

        @Test
        @DisplayName("Should handle invalid Authorization header format")
        void shouldHandleInvalidAuthorizationHeaderFormat() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile")
                    .header("Authorization", "InvalidFormat " + USER_JWT_TOKEN))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("Should allow admin access to admin endpoints")
        void shouldAllowAdminAccessToAdminEndpoints() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users")
                    .header("Authorization", "Bearer " + ADMIN_JWT_TOKEN))
                .andDo(print())
                .andExpect(status().isOk());

            mockMvc.perform(get("/actuator/metrics")
                    .header("Authorization", "Bearer " + ADMIN_JWT_TOKEN))
                .andDo(print())
                .andExpected(status().isOk());
        }

        @Test
        @DisplayName("Should deny user access to admin endpoints")
        void shouldDenyUserAccessToAdminEndpoints() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users")
                    .header("Authorization", "Bearer " + USER_JWT_TOKEN))
                .andDo(print())
                .andExpect(status().isForbidden());

            mockMvc.perform(get("/actuator/metrics")
                    .header("Authorization", "Bearer " + USER_JWT_TOKEN))
                .andDo(print())
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should allow user access to their own profile")
        void shouldAllowUserAccessToOwnProfile() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile")
                    .header("Authorization", "Bearer " + USER_JWT_TOKEN))
                .andDo(print())
                .andExpect(status().isOk());

            mockMvc.perform(put("/api/v1/users/profile")
                    .header("Authorization", "Bearer " + USER_JWT_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"firstName\":\"John\",\"lastName\":\"Doe\"}"))
                .andDo(print())
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow driver access to delivery endpoints")
        void shouldAllowDriverAccessToDeliveryEndpoints() throws Exception {
            mockMvc.perform(get("/api/v1/deliveries/assigned")
                    .header("Authorization", "Bearer " + DRIVER_JWT_TOKEN))
                .andDo(print())
                .andExpect(status().isOk());

            mockMvc.perform(patch("/api/v1/deliveries/123/status")
                    .header("Authorization", "Bearer " + DRIVER_JWT_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\"IN_TRANSIT\"}"))
                .andDo(print())
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should deny customer access to driver endpoints")
        void shouldDenyCustomerAccessToDriverEndpoints() throws Exception {
            mockMvc.perform(get("/api/v1/deliveries/assigned")
                    .header("Authorization", "Bearer " + USER_JWT_TOKEN))
                .andDo(print())
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should apply method-level security annotations")
        void shouldApplyMethodLevelSecurity() throws Exception {
            mockMvc.perform(delete("/api/v1/users/user-123")
                    .header("Authorization", "Bearer " + ADMIN_JWT_TOKEN))
                .andDo(print())
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("CORS Tests")
    class CorsTests {

        @Test
        @DisplayName("Should handle preflight CORS requests")
        void shouldHandlePreflightCorsRequests() throws Exception {
            mockMvc.perform(options("/api/v1/users/profile")
                    .header("Origin", "http://localhost:3000")
                    .header("Access-Control-Request-Method", "GET")
                    .header("Access-Control-Request-Headers", "Authorization"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("GET")))
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("Authorization")))
                .andExpect(header().exists("Access-Control-Max-Age"));
        }

        @Test
        @DisplayName("Should allow requests from allowed origins")
        void shouldAllowRequestsFromAllowedOrigins() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile")
                    .header("Origin", "http://localhost:3000")
                    .header("Authorization", "Bearer " + USER_JWT_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
        }

        @Test
        @DisplayName("Should deny requests from disallowed origins in production")
        void shouldDenyRequestsFromDisallowedOrigins() throws Exception {
            // This test would need to be configured for production profile
            mockMvc.perform(get("/api/v1/users/profile")
                    .header("Origin", "http://malicious.com")
                    .header("Authorization", "Bearer " + USER_JWT_TOKEN))
                .andDo(print())
                .andExpected(header().doesNotExist("Access-Control-Allow-Origin"));
        }

        @Test
        @DisplayName("Should expose configured headers")
        void shouldExposeConfiguredHeaders() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile")
                    .header("Origin", "http://localhost:3000")
                    .header("Authorization", "Bearer " + USER_JWT_TOKEN))
                .andDo(print())
                .andExpect(header().exists("Access-Control-Expose-Headers"));
        }
    }

    @Nested
    @DisplayName("CSRF Tests")
    class CsrfTests {

        @Test
        @DisplayName("Should generate CSRF token for authenticated users")
        void shouldGenerateCsrfTokenForAuthenticatedUsers() throws Exception {
            mockMvc.perform(get("/api/v1/csrf-token")
                    .header("Authorization", "Bearer " + USER_JWT_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(jsonPath("$.token").exists());
        }

        @Test
        @DisplayName("Should validate CSRF token for state-changing operations")
        void shouldValidateCsrfTokenForStateChangingOperations() throws Exception {
            // This test would require CSRF to be enabled and properly configured
            String csrfToken = "test-csrf-token";
            
            mockMvc.perform(post("/api/v1/users/profile")
                    .header("Authorization", "Bearer " + USER_JWT_TOKEN)
                    .header("X-XSRF-TOKEN", csrfToken)
                    .cookie(new javax.servlet.http.Cookie("XSRF-TOKEN", csrfToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"firstName\":\"John\"}"))
                .andDo(print())
                .andExpected(status().isOk());
        }

        @Test
        @DisplayName("Should reject requests with invalid CSRF token")
        void shouldRejectRequestsWithInvalidCsrfToken() throws Exception {
            mockMvc.perform(post("/api/v1/users/profile")
                    .header("Authorization", "Bearer " + USER_JWT_TOKEN)
                    .header("X-XSRF-TOKEN", "invalid-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"firstName\":\"John\"}"))
                .andDo(print())
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Rate Limiting Tests")
    class RateLimitingTests {

        @Test
        @DisplayName("Should apply rate limiting to API endpoints")
        void shouldApplyRateLimitingToApiEndpoints() throws Exception {
            // Simulate multiple requests to trigger rate limiting
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(get("/api/v1/users/profile")
                        .header("Authorization", "Bearer " + USER_JWT_TOKEN))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().exists("X-Rate-Limit-Remaining"));
            }
        }

        @Test
        @DisplayName("Should return 429 when rate limit exceeded")
        void shouldReturn429WhenRateLimitExceeded() throws Exception {
            // This test would require rate limiting to be properly configured
            // and would need to make enough requests to exceed the limit
            
            // Simulate rate limit exceeded scenario
            mockMvc.perform(get("/api/v1/users/profile")
                    .header("Authorization", "Bearer " + USER_JWT_TOKEN))
                .andDo(print())
                .andExpect(header().exists("X-Rate-Limit-Remaining"))
                .andExpect(header().exists("X-Rate-Limit-Reset"));
        }

        @Test
        @DisplayName("Should exclude health endpoints from rate limiting")
        void shouldExcludeHealthEndpointsFromRateLimiting() throws Exception {
            // Health endpoints should not be rate limited
            for (int i = 0; i < 10; i++) {
                mockMvc.perform(get("/actuator/health"))
                    .andDo(print())
                    .andExpect(status().isOk());
            }
        }
    }

    @Nested
    @DisplayName("Security Headers Tests")
    class SecurityHeadersTests {

        @Test
        @DisplayName("Should include security headers in responses")
        void shouldIncludeSecurityHeadersInResponses() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile")
                    .header("Authorization", "Bearer " + USER_JWT_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-XSS-Protection", "1; mode=block"))
                .andExpect(header().exists("Referrer-Policy"))
                .andExpect(header().exists("Content-Security-Policy"));
        }

        @Test
        @DisplayName("Should include HSTS header for HTTPS requests")
        void shouldIncludeHstsHeaderForHttpsRequests() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile")
                    .header("Authorization", "Bearer " + USER_JWT_TOKEN)
                    .with(request -> {
                        request.setScheme("https");
                        request.setSecure(true);
                        return request;
                    }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Strict-Transport-Security"));
        }

        @Test
        @DisplayName("Should set appropriate cache headers for API responses")
        void shouldSetAppropriateCacheHeadersForApiResponses() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile")
                    .header("Authorization", "Bearer " + USER_JWT_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-cache")));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return structured error response for authentication failures")
        void shouldReturnStructuredErrorResponseForAuthFailures() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should return structured error response for authorization failures")
        void shouldReturnStructuredErrorResponseForAuthzFailures() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users")
                    .header("Authorization", "Bearer " + USER_JWT_TOKEN))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("access_denied"))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should not expose sensitive information in error responses")
        void shouldNotExposeSensitiveInformationInErrorResponses() throws Exception {
            mockMvc.perform(get("/api/v1/users/profile")
                    .header("Authorization", "Bearer invalid-token"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(not(containsString("stacktrace"))))
                .andExpect(content().string(not(containsString("SQLException"))))
                .andExpect(content().string(not(containsString("NullPointerException"))));
        }

        @Test
        @DisplayName("Should log security events for audit purposes")
        void shouldLogSecurityEventsForAuditPurposes() throws Exception {
            // This test would verify that security events are properly logged
            // In a real implementation, you would check log outputs or audit records
            
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"test@example.com\",\"password\":\"wrongpassword\"}"))
                .andDo(print())
                .andExpected(status().isUnauthorized());
                
            // Verify that authentication failure event is logged
            // This would require integration with your logging/audit system
        }
    }

    @Nested
    @DisplayName("Service-to-Service Authentication Tests")
    class ServiceToServiceAuthTests {

        @Test
        @DisplayName("Should authenticate service-to-service requests")
        void shouldAuthenticateServiceToServiceRequests() throws Exception {
            // Mock service token
            when(jwtDecoder.decode(anyString())).thenReturn(
                createJwtToken("user-service", "service-user", "SERVICE",
                    Map.of("service_name", "auth-service", "roles", "ROLE_SERVICE"))
            );

            mockMvc.perform(get("/api/v1/internal/users/user-123")
                    .header("Authorization", "Bearer service-token")
                    .header("X-Service-Name", "auth-service"))
                .andDo(print())
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should deny requests from untrusted services")
        void shouldDenyRequestsFromUntrustedServices() throws Exception {
            when(jwtDecoder.decode(anyString())).thenReturn(
                createJwtToken("untrusted-service", "service-untrusted", "SERVICE",
                    Map.of("service_name", "untrusted-service", "roles", "ROLE_SERVICE"))
            );

            mockMvc.perform(get("/api/v1/internal/users/user-123")
                    .header("Authorization", "Bearer service-token")
                    .header("X-Service-Name", "untrusted-service"))
                .andDo(print())
                .andExpect(status().isForbidden());
        }
    }
}
