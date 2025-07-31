package com.doordash.notification_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for storing device tokens for push notifications.
 * Supports multiple devices per user and different platforms.
 */
@Entity
@Table(name = "device_tokens", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "device_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "token", nullable = false, length = 500)
    private String token;

    @Column(name = "platform", nullable = false, length = 20)
    private String platform; // 'ios', 'android', 'web'

    @Column(name = "app_version", length = 50)
    private String appVersion;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_used_at")
    @Builder.Default
    private LocalDateTime lastUsedAt = LocalDateTime.now();

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
     * Platform constants
     */
    public static class Platform {
        public static final String IOS = "ios";
        public static final String ANDROID = "android";
        public static final String WEB = "web";
    }

    /**
     * Update the last used timestamp
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Deactivate the device token
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Activate the device token
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if the token is valid for use
     */
    public boolean isValidForUse() {
        return this.isActive && 
               this.token != null && 
               !this.token.trim().isEmpty();
    }

    /**
     * Create a new device token
     */
    public static DeviceToken create(Long userId, String deviceId, String token, String platform, String appVersion) {
        return DeviceToken.builder()
                .userId(userId)
                .deviceId(deviceId)
                .token(token)
                .platform(platform)
                .appVersion(appVersion)
                .isActive(true)
                .build();
    }
}
