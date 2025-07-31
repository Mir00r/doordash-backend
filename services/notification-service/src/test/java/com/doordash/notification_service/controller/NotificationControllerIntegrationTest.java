package com.doordash.notification_service.controller;

import com.doordash.notification_service.dto.NotificationRequestDTO;
import com.doordash.notification_service.entity.Notification;
import com.doordash.notification_service.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for NotificationController.
 * Tests the REST API endpoints for notification management.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAndSendNotification() throws Exception {
        // Given
        NotificationRequestDTO request = NotificationRequestDTO.builder()
                .userId(1L)
                .type(Notification.NotificationType.EMAIL)
                .channel("email")
                .recipient("test@example.com")
                .subject("Test Subject")
                .content("Test Content")
                .priority(5)
                .maxRetries(3)
                .build();

        // When & Then
        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAndSendNotificationWithInvalidData() throws Exception {
        // Given - invalid request with missing required fields
        NotificationRequestDTO request = NotificationRequestDTO.builder()
                .type(Notification.NotificationType.EMAIL)
                .build();

        // When & Then
        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateNotificationWithoutAuthentication() throws Exception {
        // Given
        NotificationRequestDTO request = NotificationRequestDTO.builder()
                .userId(1L)
                .type(Notification.NotificationType.EMAIL)
                .channel("email")
                .recipient("test@example.com")
                .subject("Test Subject")
                .content("Test Content")
                .build();

        // When & Then
        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateNotificationWithInsufficientRole() throws Exception {
        // Given
        NotificationRequestDTO request = NotificationRequestDTO.builder()
                .userId(2L) // Different user ID
                .type(Notification.NotificationType.EMAIL)
                .channel("email")
                .recipient("test@example.com")
                .subject("Test Subject")
                .content("Test Content")
                .build();

        // When & Then
        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
