package com.doordash.notification_service.service;

import com.doordash.notification_service.entity.Notification;

/**
 * Service interface for notification delivery providers.
 * Abstracts the delivery mechanism for different notification types.
 */
public interface NotificationDeliveryService {

    /**
     * Send notification through appropriate provider
     */
    void sendNotification(Notification notification);

    /**
     * Send email notification
     */
    void sendEmailNotification(Notification notification);

    /**
     * Send SMS notification
     */
    void sendSmsNotification(Notification notification);

    /**
     * Send push notification
     */
    void sendPushNotification(Notification notification);

    /**
     * Send WebSocket notification
     */
    void sendWebSocketNotification(Notification notification);

    /**
     * Send in-app notification
     */
    void sendInAppNotification(Notification notification);

    /**
     * Check if provider is available for notification type
     */
    boolean isProviderAvailable(Notification.NotificationType type);

    /**
     * Get provider health status
     */
    boolean isProviderHealthy(Notification.ProviderType providerType);
}
