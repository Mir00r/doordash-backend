package com.doordash.notification_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity representing user notification preferences.
 * Controls which types of notifications a user wants to receive and how.
 */
@Entity
@Table(name = "user_notification_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "email_enabled")
    @Builder.Default
    private Boolean emailEnabled = true;

    @Column(name = "sms_enabled")
    @Builder.Default
    private Boolean smsEnabled = true;

    @Column(name = "push_enabled")
    @Builder.Default
    private Boolean pushEnabled = true;

    @Column(name = "in_app_enabled")
    @Builder.Default
    private Boolean inAppEnabled = true;

    @Column(name = "marketing_emails")
    @Builder.Default
    private Boolean marketingEmails = false;

    @Column(name = "order_updates")
    @Builder.Default
    private Boolean orderUpdates = true;

    @Column(name = "delivery_updates")
    @Builder.Default
    private Boolean deliveryUpdates = true;

    @Column(name = "promotional_notifications")
    @Builder.Default
    private Boolean promotionalNotifications = false;

    @Column(name = "time_zone")
    @Builder.Default
    private String timeZone = "UTC";

    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;

    @Column(name = "preferred_language")
    @Builder.Default
    private String preferredLanguage = "en";

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if the user has enabled notifications for the given type
     */
    public boolean isNotificationTypeEnabled(Notification.NotificationType type) {
        return switch (type) {
            case EMAIL -> this.emailEnabled;
            case SMS -> this.smsEnabled;
            case PUSH -> this.pushEnabled;
            case IN_APP -> this.inAppEnabled;
            case WEBSOCKET -> true; // WebSocket is always enabled for real-time updates
        };
    }

    /**
     * Check if marketing notifications are enabled
     */
    public boolean canReceiveMarketingNotifications() {
        return this.marketingEmails || this.promotionalNotifications;
    }

    /**
     * Check if order-related notifications are enabled
     */
    public boolean canReceiveOrderNotifications() {
        return this.orderUpdates;
    }

    /**
     * Check if delivery-related notifications are enabled
     */
    public boolean canReceiveDeliveryNotifications() {
        return this.deliveryUpdates;
    }

    /**
     * Check if current time is within quiet hours
     */
    public boolean isInQuietHours() {
        if (quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }
        
        LocalTime now = LocalTime.now();
        
        if (quietHoursStart.isBefore(quietHoursEnd)) {
            // Same day quiet hours (e.g., 22:00 - 07:00)
            return now.isAfter(quietHoursStart) && now.isBefore(quietHoursEnd);
        } else {
            // Overnight quiet hours (e.g., 22:00 - 07:00 next day)
            return now.isAfter(quietHoursStart) || now.isBefore(quietHoursEnd);
        }
    }

    /**
     * Create default preferences for a user
     */
    public static UserNotificationPreference createDefault(Long userId) {
        return UserNotificationPreference.builder()
                .userId(userId)
                .emailEnabled(true)
                .smsEnabled(true)
                .pushEnabled(true)
                .inAppEnabled(true)
                .marketingEmails(false)
                .orderUpdates(true)
                .deliveryUpdates(true)
                .promotionalNotifications(false)
                .timeZone("UTC")
                .preferredLanguage("en")
                .build();
    }
}
