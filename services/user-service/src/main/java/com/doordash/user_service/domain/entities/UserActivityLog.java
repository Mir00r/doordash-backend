package com.doordash.user_service.domain.entities;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * UserActivityLog entity for tracking user activities and analytics.
 * 
 * This entity logs all significant user activities for security monitoring,
 * analytics, and user behavior analysis.
 * 
 * Features:
 * - Comprehensive activity tracking
 * - Device and location information
 * - Metadata storage for custom activity data
 * - Security monitoring with IP and user agent tracking
 * - Analytics support for user behavior patterns
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@Entity
@Table(name = "user_activity_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @Column(name = "activity_description", columnDefinition = "TEXT")
    private String activityDescription;

    @Column(name = "ip_address", columnDefinition = "inet")
    private InetAddress ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 50)
    private DeviceType deviceType;

    @Type(JsonType.class)
    @Column(name = "location_data", columnDefinition = "jsonb")
    private Map<String, Object> locationData;

    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Enumeration for activity types.
     */
    public enum ActivityType {
        // Authentication activities
        LOGIN,
        LOGOUT,
        PASSWORD_CHANGE,
        EMAIL_VERIFICATION,
        
        // Profile activities
        PROFILE_UPDATE,
        PROFILE_VIEW,
        AVATAR_UPLOAD,
        
        // Address activities
        ADDRESS_ADD,
        ADDRESS_UPDATE,
        ADDRESS_DELETE,
        ADDRESS_SET_DEFAULT,
        
        // Preference activities
        PREFERENCES_UPDATE,
        NOTIFICATION_SETTINGS_CHANGE,
        
        // Order activities
        ORDER_PLACED,
        ORDER_CANCELLED,
        ORDER_VIEWED,
        ORDER_RATED,
        
        // Restaurant activities
        RESTAURANT_VIEWED,
        RESTAURANT_FAVORITED,
        RESTAURANT_UNFAVORITED,
        MENU_VIEWED,
        
        // Search activities
        SEARCH_PERFORMED,
        FILTER_APPLIED,
        
        // Payment activities
        PAYMENT_METHOD_ADDED,
        PAYMENT_METHOD_UPDATED,
        PAYMENT_METHOD_DELETED,
        
        // App activities
        APP_OPENED,
        APP_CLOSED,
        PUSH_NOTIFICATION_RECEIVED,
        PUSH_NOTIFICATION_CLICKED,
        
        // Security activities
        SUSPICIOUS_ACTIVITY,
        ACCOUNT_LOCKED,
        ACCOUNT_UNLOCKED,
        TWO_FACTOR_ENABLED,
        TWO_FACTOR_DISABLED,
        
        // Other
        OTHER
    }

    /**
     * Enumeration for device types.
     */
    public enum DeviceType {
        WEB,
        MOBILE_IOS,
        MOBILE_ANDROID,
        TABLET_IOS,
        TABLET_ANDROID,
        DESKTOP_APP,
        API_CLIENT,
        UNKNOWN
    }

    /**
     * Check if this is a security-related activity.
     * 
     * @return true if the activity is security-related
     */
    public boolean isSecurityActivity() {
        return activityType == ActivityType.LOGIN ||
               activityType == ActivityType.LOGOUT ||
               activityType == ActivityType.PASSWORD_CHANGE ||
               activityType == ActivityType.SUSPICIOUS_ACTIVITY ||
               activityType == ActivityType.ACCOUNT_LOCKED ||
               activityType == ActivityType.ACCOUNT_UNLOCKED ||
               activityType == ActivityType.TWO_FACTOR_ENABLED ||
               activityType == ActivityType.TWO_FACTOR_DISABLED;
    }

    /**
     * Check if this is an authentication-related activity.
     * 
     * @return true if the activity is authentication-related
     */
    public boolean isAuthenticationActivity() {
        return activityType == ActivityType.LOGIN ||
               activityType == ActivityType.LOGOUT ||
               activityType == ActivityType.PASSWORD_CHANGE ||
               activityType == ActivityType.EMAIL_VERIFICATION;
    }

    /**
     * Check if this is a profile-related activity.
     * 
     * @return true if the activity is profile-related
     */
    public boolean isProfileActivity() {
        return activityType == ActivityType.PROFILE_UPDATE ||
               activityType == ActivityType.PROFILE_VIEW ||
               activityType == ActivityType.AVATAR_UPLOAD ||
               activityType.name().startsWith("ADDRESS_") ||
               activityType.name().startsWith("PREFERENCES_");
    }

    /**
     * Check if this is an order-related activity.
     * 
     * @return true if the activity is order-related
     */
    public boolean isOrderActivity() {
        return activityType.name().startsWith("ORDER_");
    }

    /**
     * Check if this activity should trigger analytics events.
     * 
     * @return true if analytics should be triggered
     */
    public boolean shouldTriggerAnalytics() {
        return activityType == ActivityType.ORDER_PLACED ||
               activityType == ActivityType.RESTAURANT_VIEWED ||
               activityType == ActivityType.SEARCH_PERFORMED ||
               activityType == ActivityType.APP_OPENED ||
               isOrderActivity();
    }

    /**
     * Get a human-readable description of the activity.
     * 
     * @return Formatted activity description
     */
    public String getFormattedDescription() {
        if (activityDescription != null && !activityDescription.trim().isEmpty()) {
            return activityDescription;
        }
        
        return switch (activityType) {
            case LOGIN -> "User logged in";
            case LOGOUT -> "User logged out";
            case PROFILE_UPDATE -> "User updated their profile";
            case ADDRESS_ADD -> "User added a new address";
            case ORDER_PLACED -> "User placed an order";
            case RESTAURANT_VIEWED -> "User viewed a restaurant";
            case SEARCH_PERFORMED -> "User performed a search";
            default -> "User performed: " + activityType.name().toLowerCase().replace("_", " ");
        };
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
