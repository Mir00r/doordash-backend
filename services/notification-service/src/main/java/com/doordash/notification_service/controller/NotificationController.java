package com.doordash.notification_service.controller;

import com.doordash.notification_service.dto.BulkNotificationRequestDTO;
import com.doordash.notification_service.dto.NotificationRequestDTO;
import com.doordash.notification_service.dto.NotificationResponseDTO;
import com.doordash.notification_service.entity.Notification;
import com.doordash.notification_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller for notification management.
 * Provides endpoints for creating, sending, and managing notifications.
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Management", description = "APIs for managing notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Create and send a notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification created and sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSTEM') or #request.userId == authentication.principal.userId")
    public ResponseEntity<NotificationResponseDTO> createAndSendNotification(
            @Valid @RequestBody NotificationRequestDTO request) {
        log.info("Creating and sending notification for user: {}, type: {}", 
                request.getUserId(), request.getType());
        
        NotificationResponseDTO response = notificationService.createAndSendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Create a notification without sending")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/draft")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSTEM')")
    public ResponseEntity<NotificationResponseDTO> createNotification(
            @Valid @RequestBody NotificationRequestDTO request) {
        log.info("Creating draft notification for user: {}, type: {}", 
                request.getUserId(), request.getType());
        
        NotificationResponseDTO response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Send bulk notifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bulk notifications created and sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSTEM')")
    public ResponseEntity<List<NotificationResponseDTO>> sendBulkNotifications(
            @Valid @RequestBody BulkNotificationRequestDTO request) {
        log.info("Sending bulk notifications to {} users, type: {}", 
                request.getUserIds().size(), request.getType());
        
        List<NotificationResponseDTO> responses = notificationService.sendBulkNotifications(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @Operation(summary = "Send a draft notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification sent successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "400", description = "Notification cannot be sent")
    })
    @PostMapping("/{notificationId}/send")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSTEM')")
    public ResponseEntity<NotificationResponseDTO> sendNotification(
            @Parameter(description = "Notification ID") @PathVariable Long notificationId) {
        log.info("Sending notification with ID: {}", notificationId);
        
        NotificationResponseDTO response = notificationService.sendNotification(notificationId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Schedule a notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification scheduled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/schedule")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSTEM')")
    public ResponseEntity<NotificationResponseDTO> scheduleNotification(
            @Valid @RequestBody NotificationRequestDTO request,
            @Parameter(description = "Scheduled delivery time") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledAt) {
        log.info("Scheduling notification for user: {} at: {}", request.getUserId(), scheduledAt);
        
        NotificationResponseDTO response = notificationService.scheduleNotification(request, scheduledAt);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Cancel a scheduled notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "400", description = "Notification cannot be cancelled")
    })
    @PostMapping("/{notificationId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSTEM')")
    public ResponseEntity<NotificationResponseDTO> cancelNotification(
            @Parameter(description = "Notification ID") @PathVariable Long notificationId) {
        log.info("Cancelling notification with ID: {}", notificationId);
        
        NotificationResponseDTO response = notificationService.cancelNotification(notificationId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retry a failed notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification retry initiated"),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "400", description = "Notification cannot be retried")
    })
    @PostMapping("/{notificationId}/retry")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSTEM')")
    public ResponseEntity<NotificationResponseDTO> retryNotification(
            @Parameter(description = "Notification ID") @PathVariable Long notificationId) {
        log.info("Retrying notification with ID: {}", notificationId);
        
        NotificationResponseDTO response = notificationService.retryNotification(notificationId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get notification by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification found"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @GetMapping("/{notificationId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSTEM')")
    public ResponseEntity<NotificationResponseDTO> getNotification(
            @Parameter(description = "Notification ID") @PathVariable Long notificationId) {
        log.debug("Getting notification with ID: {}", notificationId);
        
        return notificationService.getNotificationById(notificationId)
                .map(notification -> ResponseEntity.ok(notification))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get notifications for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSTEM') or #userId == authentication.principal.userId")
    public ResponseEntity<Page<NotificationResponseDTO>> getUserNotifications(
            @Parameter(description = "User ID") @PathVariable Long userId,
            Pageable pageable) {
        log.debug("Getting notifications for user: {}", userId);
        
        Page<NotificationResponseDTO> notifications = notificationService.getNotificationsForUser(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get notifications by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully")
    })
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSTEM')")
    public ResponseEntity<List<NotificationResponseDTO>> getNotificationsByStatus(
            @Parameter(description = "Notification status") @PathVariable Notification.NotificationStatus status) {
        log.debug("Getting notifications with status: {}", status);
        
        List<NotificationResponseDTO> notifications = notificationService.getNotificationsByStatus(status);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get notification statistics for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @GetMapping("/user/{userId}/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSTEM') or #userId == authentication.principal.userId")
    public ResponseEntity<Map<String, Long>> getUserNotificationStats(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        log.debug("Getting notification statistics for user: {}", userId);
        
        Map<String, Long> stats = notificationService.getNotificationStatsForUser(userId);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get notification statistics by type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Map<String, Long>>> getNotificationStatsByType(
            @Parameter(description = "Start date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "End date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.debug("Getting notification statistics from {} to {}", start, end);
        
        Map<String, Map<String, Long>> stats = notificationService.getNotificationStatsByType(start, end);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get unread in-app notification count")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @GetMapping("/user/{userId}/unread-count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSTEM') or #userId == authentication.principal.userId")
    public ResponseEntity<Map<String, Long>> getUnreadNotificationCount(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        log.debug("Getting unread notification count for user: {}", userId);
        
        long count = notificationService.countUnreadInAppNotifications(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
}
