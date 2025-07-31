package com.doordash.user_service.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * UserPreferences entity representing user preferences and settings.
 * 
 * This entity stores comprehensive user preferences including notification settings,
 * dietary restrictions, cuisine preferences, and app configurations.
 * 
 * Features:
 * - Notification preferences (email, SMS, push, marketing)
 * - Dietary restrictions and cuisine preferences stored as JSON
 * - Localization settings (language, currency, timezone)
 * - Delivery preferences and default settings
 * - App UI preferences (dark mode, etc.)
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@Entity
@Table(name = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Builder.Default
    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage = "en";

    @Builder.Default
    @Column(name = "preferred_currency", length = 3)
    private String preferredCurrency = "USD";

    @Builder.Default
    @Column(name = "timezone", length = 50)
    private String timezone = "UTC";

    @Builder.Default
    @Column(name = "notification_email", nullable = false)
    private Boolean notificationEmail = true;

    @Builder.Default
    @Column(name = "notification_sms", nullable = false)
    private Boolean notificationSms = true;

    @Builder.Default
    @Column(name = "notification_push", nullable = false)
    private Boolean notificationPush = true;

    @Builder.Default
    @Column(name = "notification_marketing", nullable = false)
    private Boolean notificationMarketing = false;

    @Type(JsonType.class)
    @Column(name = "dietary_restrictions", columnDefinition = "jsonb")
    private List<String> dietaryRestrictions = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "cuisine_preferences", columnDefinition = "jsonb")
    private List<String> cuisinePreferences = new ArrayList<>();

    @Builder.Default
    @Column(name = "max_delivery_distance")
    private Integer maxDeliveryDistance = 10; // in miles

    @Builder.Default
    @Column(name = "default_tip_percentage")
    private Integer defaultTipPercentage = 15;

    @Builder.Default
    @Column(name = "auto_reorder_enabled", nullable = false)
    private Boolean autoReorderEnabled = false;

    @Builder.Default
    @Column(name = "dark_mode_enabled", nullable = false)
    private Boolean darkModeEnabled = false;

    @Builder.Default
    @Column(name = "location_sharing_enabled", nullable = false)
    private Boolean locationSharingEnabled = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * Check if user has any dietary restrictions.
     * 
     * @return true if user has dietary restrictions
     */
    public boolean hasDietaryRestrictions() {
        return dietaryRestrictions != null && !dietaryRestrictions.isEmpty();
    }

    /**
     * Check if user has cuisine preferences.
     * 
     * @return true if user has cuisine preferences
     */
    public boolean hasCuisinePreferences() {
        return cuisinePreferences != null && !cuisinePreferences.isEmpty();
    }

    /**
     * Add a dietary restriction if not already present.
     * 
     * @param restriction The dietary restriction to add
     */
    public void addDietaryRestriction(String restriction) {
        if (dietaryRestrictions == null) {
            dietaryRestrictions = new ArrayList<>();
        }
        if (!dietaryRestrictions.contains(restriction)) {
            dietaryRestrictions.add(restriction);
        }
    }

    /**
     * Remove a dietary restriction.
     * 
     * @param restriction The dietary restriction to remove
     */
    public void removeDietaryRestriction(String restriction) {
        if (dietaryRestrictions != null) {
            dietaryRestrictions.remove(restriction);
        }
    }

    /**
     * Add a cuisine preference if not already present.
     * 
     * @param cuisine The cuisine preference to add
     */
    public void addCuisinePreference(String cuisine) {
        if (cuisinePreferences == null) {
            cuisinePreferences = new ArrayList<>();
        }
        if (!cuisinePreferences.contains(cuisine)) {
            cuisinePreferences.add(cuisine);
        }
    }

    /**
     * Remove a cuisine preference.
     * 
     * @param cuisine The cuisine preference to remove
     */
    public void removeCuisinePreference(String cuisine) {
        if (cuisinePreferences != null) {
            cuisinePreferences.remove(cuisine);
        }
    }

    /**
     * Check if user wants any notifications.
     * 
     * @return true if any notification type is enabled
     */
    public boolean wantsAnyNotifications() {
        return Boolean.TRUE.equals(notificationEmail) || 
               Boolean.TRUE.equals(notificationSms) || 
               Boolean.TRUE.equals(notificationPush);
    }

    /**
     * Get notification channels that are enabled.
     * 
     * @return List of enabled notification channels
     */
    public List<String> getEnabledNotificationChannels() {
        List<String> channels = new ArrayList<>();
        if (Boolean.TRUE.equals(notificationEmail)) {
            channels.add("EMAIL");
        }
        if (Boolean.TRUE.equals(notificationSms)) {
            channels.add("SMS");
        }
        if (Boolean.TRUE.equals(notificationPush)) {
            channels.add("PUSH");
        }
        return channels;
    }

    /**
     * Validate tip percentage is within reasonable bounds.
     * 
     * @return true if tip percentage is valid
     */
    public boolean isValidTipPercentage() {
        return defaultTipPercentage != null && 
               defaultTipPercentage >= 0 && 
               defaultTipPercentage <= 50;
    }

    /**
     * Validate delivery distance is within reasonable bounds.
     * 
     * @return true if delivery distance is valid
     */
    public boolean isValidDeliveryDistance() {
        return maxDeliveryDistance != null && 
               maxDeliveryDistance >= 1 && 
               maxDeliveryDistance <= 50;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (dietaryRestrictions == null) {
            dietaryRestrictions = new ArrayList<>();
        }
        if (cuisinePreferences == null) {
            cuisinePreferences = new ArrayList<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
