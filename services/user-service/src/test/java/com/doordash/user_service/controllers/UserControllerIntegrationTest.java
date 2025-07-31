package com.doordash.user_service.controllers;

import com.doordash.user_service.domain.dtos.user.CreateUserProfileRequest;
import com.doordash.user_service.domain.dtos.user.UpdateUserProfileRequest;
import com.doordash.user_service.domain.dtos.user.UserProfileResponse;
import com.doordash.user_service.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController.
 * 
 * Tests all REST endpoints for user profile management including
 * authentication, authorization, validation, and business logic.
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@WebMvcTest(UserController.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private CreateUserProfileRequest createRequest;
    private UpdateUserProfileRequest updateRequest;
    private UserProfileResponse profileResponse;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        createRequest = CreateUserProfileRequest.builder()
                .userId(testUserId)
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .bio("Food enthusiast from San Francisco")
                .build();

        updateRequest = UpdateUserProfileRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("+9876543210")
                .bio("Updated bio")
                .build();

        profileResponse = UserProfileResponse.builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .firstName("John")
                .lastName("Doe")
                .displayName("John Doe")
                .fullName("John Doe")
                .phoneNumber("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .bio("Food enthusiast from San Francisco")
                .isActive(true)
                .isVerified(false)
                .isComplete(true)
                .isEligibleForAdvancedFeatures(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"USER"})
    void createProfile_Success() throws Exception {
        // Given
        when(userService.createProfile(any(CreateUserProfileRequest.class)))
                .thenReturn(profileResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/users/profiles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpected(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(userService, times(1)).createProfile(any(CreateUserProfileRequest.class));
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"USER"})
    void createProfile_InvalidRequest_BadRequest() throws Exception {
        // Given - Invalid request with missing required fields
        CreateUserProfileRequest invalidRequest = CreateUserProfileRequest.builder()
                .userId(testUserId)
                .firstName("") // Empty first name
                .lastName("Doe")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/users/profiles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createProfile(any(CreateUserProfileRequest.class));
    }

    @Test
    void createProfile_Unauthorized() throws Exception {
        // When & Then - No authentication
        mockMvc.perform(post("/api/v1/users/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).createProfile(any(CreateUserProfileRequest.class));
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"USER"})
    void getProfile_Success() throws Exception {
        // Given
        when(userService.getProfile(testUserId))
                .thenReturn(Optional.of(profileResponse));

        // When & Then
        mockMvc.perform(get("/api/v1/users/profiles/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(userService, times(1)).getProfile(testUserId);
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"USER"})
    void getProfile_NotFound() throws Exception {
        // Given
        when(userService.getProfile(testUserId))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/users/profiles/{userId}", testUserId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getProfile(testUserId);
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"USER"})
    void getCurrentUserProfile_Success() throws Exception {
        // Given
        UUID currentUserId = UUID.fromString("test-user");
        when(userService.getProfile(currentUserId))
                .thenReturn(Optional.of(profileResponse));

        // When & Then
        mockMvc.perform(get("/api/v1/users/profiles/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(userService, times(1)).getProfile(currentUserId);
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"USER"})
    void updateProfile_Success() throws Exception {
        // Given
        UserProfileResponse updatedResponse = profileResponse.toBuilder()
                .firstName("Jane")
                .lastName("Smith")
                .build();

        when(userService.updateProfile(eq(testUserId), any(UpdateUserProfileRequest.class)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/users/profiles/{userId}", testUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpected(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));

        verify(userService, times(1)).updateProfile(eq(testUserId), any(UpdateUserProfileRequest.class));
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"USER"})
    void updateProfile_InvalidRequest_BadRequest() throws Exception {
        // Given - Invalid request with too long first name
        UpdateUserProfileRequest invalidRequest = UpdateUserProfileRequest.builder()
                .firstName("A".repeat(101)) // Too long
                .lastName("Smith")
                .build();

        // When & Then
        mockMvc.perform(put("/api/v1/users/profiles/{userId}", testUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateProfile(any(UUID.class), any(UpdateUserProfileRequest.class));
    }

    @Test
    @WithMockUser(username = "different-user", roles = {"USER"})
    void updateProfile_DifferentUser_Forbidden() throws Exception {
        // When & Then - User trying to update another user's profile
        mockMvc.perform(put("/api/v1/users/profiles/{userId}", testUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        verify(userService, never()).updateProfile(any(UUID.class), any(UpdateUserProfileRequest.class));
    }

    @Test
    @WithMockUser(username = "admin-user", roles = {"ADMIN"})
    void updateProfile_Admin_Success() throws Exception {
        // Given - Admin user can update any profile
        UserProfileResponse updatedResponse = profileResponse.toBuilder()
                .firstName("Jane")
                .lastName("Smith")
                .build();

        when(userService.updateProfile(eq(testUserId), any(UpdateUserProfileRequest.class)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/users/profiles/{userId}", testUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Jane"));

        verify(userService, times(1)).updateProfile(eq(testUserId), any(UpdateUserProfileRequest.class));
    }

    @Test
    @WithMockUser(username = "admin-user", roles = {"ADMIN"})
    void searchProfiles_Admin_Success() throws Exception {
        // Given
        String searchTerm = "John";
        // Note: In a real test, you'd mock the Page response properly
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/profiles/search")
                        .param("searchTerm", searchTerm)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());

        verify(userService, times(1)).searchProfiles(eq(searchTerm), any());
    }

    @Test
    @WithMockUser(username = "regular-user", roles = {"USER"})
    void searchProfiles_RegularUser_Forbidden() throws Exception {
        // When & Then - Regular user cannot search profiles
        mockMvc.perform(get("/api/v1/users/profiles/search")
                        .param("searchTerm", "John"))
                .andExpect(status().isForbidden());

        verify(userService, never()).searchProfiles(anyString(), any());
    }

    @Test
    @WithMockUser(username = "admin-user", roles = {"ADMIN"})
    void deactivateProfile_Admin_Success() throws Exception {
        // Given
        doNothing().when(userService).deactivateProfile(testUserId, "admin-user");

        // When & Then
        mockMvc.perform(delete("/api/v1/users/profiles/{userId}", testUserId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deactivateProfile(testUserId, "admin-user");
    }

    @Test
    @WithMockUser(username = "regular-user", roles = {"USER"})
    void deactivateProfile_RegularUser_Forbidden() throws Exception {
        // When & Then - Regular user cannot deactivate profiles
        mockMvc.perform(delete("/api/v1/users/profiles/{userId}", testUserId)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(userService, never()).deactivateProfile(any(UUID.class), anyString());
    }

    // Additional tests for validation scenarios
    @Test
    @WithMockUser(username = "test-user", roles = {"USER"})
    void createProfile_InvalidPhoneNumber_BadRequest() throws Exception {
        // Given - Invalid phone number format
        CreateUserProfileRequest invalidRequest = createRequest.toBuilder()
                .phoneNumber("invalid-phone")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/users/profiles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createProfile(any(CreateUserProfileRequest.class));
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"USER"})
    void createProfile_FutureDateOfBirth_BadRequest() throws Exception {
        // Given - Future date of birth
        CreateUserProfileRequest invalidRequest = createRequest.toBuilder()
                .dateOfBirth(LocalDate.now().plusDays(1))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/users/profiles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpected(status().isBadRequest());

        verify(userService, never()).createProfile(any(CreateUserProfileRequest.class));
    }
}
