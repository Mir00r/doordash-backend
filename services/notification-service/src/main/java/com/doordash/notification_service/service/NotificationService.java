package com.doordash.notification_service.service;

import com.doordash.notification_service.dto.BulkNotificationRequestDTO;
import com.doordash.notification_service.dto.NotificationRequestDTO;
import com.doordash.notification_service.dto.NotificationResponseDTO;
import com.doordash.notification_service.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for notification management.
 * Provides methods for creating, sending, and managing notifications.
 */
public interface NotificationService {

    /**
     * Create and send a single notification
     */
    NotificationResponseDTO createAndSendNotification(NotificationRequestDTO request);

    /**
     * Create a notification without sending it immediately
     */
    NotificationResponseDTO createNotification(NotificationRequestDTO request);

    /**
     * Send a notification that was created earlier
     */
    NotificationResponseDTO sendNotification(Long notificationId);

    /**
     * Send bulk notifications
     */
    List<NotificationResponseDTO> sendBulkNotifications(BulkNotificationRequestDTO request);

    /**
     * Create notification from template
     */
    NotificationResponseDTO createNotificationFromTemplate(
            String templateName, 
            Long userId, 
            String recipient, 
            Map<String, Object> variables);

    /**
     * Schedule a notification for future delivery
     */
    NotificationResponseDTO scheduleNotification(NotificationRequestDTO request, LocalDateTime scheduledAt);

    /**
     * Cancel a scheduled notification
     */
    NotificationResponseDTO cancelNotification(Long notificationId);

    /**
     * Retry a failed notification
     */
    NotificationResponseDTO retryNotification(Long notificationId);

    /**
     * Get notification by ID
     */
    Optional<NotificationResponseDTO> getNotificationById(Long id);

    /**
     * Get notifications for a user with pagination
     */
    Page<NotificationResponseDTO> getNotificationsForUser(Long userId, Pageable pageable);

    /**
     * Get notifications by status
     */
    List<NotificationResponseDTO> getNotificationsByStatus(Notification.NotificationStatus status);

    /**
     * Get pending notifications ready to be sent
     */
    List<NotificationResponseDTO> getPendingNotificationsReadyToSend();

    /**
     * Get failed notifications that can be retried
     */
    List<NotificationResponseDTO> getRetryableNotifications();

    /**
     * Mark notification as delivered (called by webhook)
     */
    void markAsDelivered(String providerMessageId);

    /**
     * Mark notification as opened (called by webhook)
     */
    void markAsOpened(String providerMessageId, String userAgent, String ipAddress);

    /**
     * Mark notification as clicked (called by webhook)
     */
    void markAsClicked(String providerMessageId, String userAgent, String ipAddress);

    /**
     * Mark notification as bounced (called by webhook)
     */
    void markAsBounced(String providerMessageId, String reason);

    /**
     * Mark notification as failed
     */
    void markAsFailed(Long notificationId, String reason);

    /**
     * Get notification statistics for a user
     */
    Map<String, Long> getNotificationStatsForUser(Long userId);

    /**
     * Get notification statistics by type in date range
     */
    Map<String, Map<String, Long>> getNotificationStatsByType(LocalDateTime start, LocalDateTime end);

    /**
     * Count unread in-app notifications for user
     */
    long countUnreadInAppNotifications(Long userId);

    /**
     * Process scheduled notifications
     */
    void processScheduledNotifications();

    /**
     * Process retry notifications
     */
    void processRetryNotifications();

    /**
     * Clean up old notifications
     */
    void cleanupOldNotifications(LocalDateTime cutoffDate);
}
