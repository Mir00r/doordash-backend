package com.doordash.auth_service.controllers;

import com.doordash.auth_service.domain.dtos.auth.LoginRequest;
import com.doordash.auth_service.domain.dtos.auth.RegisterRequest;
import com.doordash.auth_service.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 * 
 * Tests the authentication endpoints including registration, login,
 * and other auth-related operations. Uses Spring Boot test features
 * for comprehensive testing with security context.
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("Auth Controller Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private AuthService authService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should register user successfully with valid data")
    void shouldRegisterUserSuccessfully() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("john.doe@example.com")
                .username("johndoe")
                .password("StrongPass123!")
                .confirmPassword("StrongPass123!")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .role("CUSTOMER")
                .acceptTerms(true)
                .acceptPrivacyPolicy(true)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should reject registration with invalid email")
    void shouldRejectRegistrationWithInvalidEmail() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("invalid-email")
                .username("johndoe")
                .password("StrongPass123!")
                .confirmPassword("StrongPass123!")
                .firstName("John")
                .lastName("Doe")
                .acceptTerms(true)
                .acceptPrivacyPolicy(true)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject registration with weak password")
    void shouldRejectRegistrationWithWeakPassword() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("john.doe@example.com")
                .username("johndoe")
                .password("weak")
                .confirmPassword("weak")
                .firstName("John")
                .lastName("Doe")
                .acceptTerms(true)
                .acceptPrivacyPolicy(true)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should login user successfully with valid credentials")
    void shouldLoginUserSuccessfully() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("john.doe@example.com")
                .password("StrongPass123!")
                .rememberMe(false)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should reject login with empty credentials")
    void shouldRejectLoginWithEmptyCredentials() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("")
                .password("")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should access public endpoints without authentication")
    void shouldAccessPublicEndpoints() throws Exception {
        // Test API documentation endpoints
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"user.read"})
    @DisplayName("Should access protected endpoint with authentication")
    void shouldAccessProtectedEndpointWithAuth() throws Exception {
        mockMvc.perform(post("/api/v1/auth/profile"))
                .andExpect(status().isMethodNotAllowed()); // GET method expected
    }

    @Test
    @DisplayName("Should reject protected endpoint without authentication")
    void shouldRejectProtectedEndpointWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/v1/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"oldPassword\":\"old\",\"newPassword\":\"new\"}"))
                .andExpect(status().isUnauthorized());
    }
}
