package com.doordash.user_service.multitenancy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Tenant Configuration Entity for DoorDash Multi-Tenant Platform.
 * 
 * Stores tenant-specific configuration settings including:
 * - Application preferences (timezone, language, currency)
 * - Security policies (password requirements, session management)
 * - Rate limiting configurations
 * - Feature flag settings
 * - Integration configurations
 * - Custom branding and themes
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "tenant_configurations", indexes = {
    @Index(name = "idx_tenant_config_tenant_id", columnList = "tenant_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    // Application Configuration
    @Column(name = "time_zone", length = 50)
    private String timeZone;

    @Column(name = "date_format", length = 20)
    private String dateFormat;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "language", length = 5)
    private String language;

    // Security Configuration
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "minLength", column = @Column(name = "pwd_min_length")),
        @AttributeOverride(name = "requireUppercase", column = @Column(name = "pwd_require_uppercase")),
        @AttributeOverride(name = "requireLowercase", column = @Column(name = "pwd_require_lowercase")),
        @AttributeOverride(name = "requireNumbers", column = @Column(name = "pwd_require_numbers")),
        @AttributeOverride(name = "requireSpecialChars", column = @Column(name = "pwd_require_special")),
        @AttributeOverride(name = "maxAge", column = @Column(name = "pwd_max_age")),
        @AttributeOverride(name = "preventReuse", column = @Column(name = "pwd_prevent_reuse"))
    })
    private PasswordPolicy passwordPolicy;

    @Column(name = "session_timeout_minutes")
    private Integer sessionTimeoutMinutes;

    @Column(name = "enable_mfa")
    private Boolean enableMfa;

    @Column(name = "enable_sso")
    private Boolean enableSso;

    @Column(name = "sso_provider", length = 50)
    private String ssoProvider;

    // Rate Limiting Configuration
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "enabled", column = @Column(name = "rate_limit_enabled")),
        @AttributeOverride(name = "defaultRequestsPerMinute", column = @Column(name = "rate_limit_default_rpm")),
        @AttributeOverride(name = "burstCapacity", column = @Column(name = "rate_limit_burst_capacity")),
        @AttributeOverride(name = "enableIpRateLimiting", column = @Column(name = "rate_limit_enable_ip")),
        @AttributeOverride(name = "enableUserRateLimiting", column = @Column(name = "rate_limit_enable_user"))
    })
    private RateLimitingConfig rateLimiting;

    // Observability Configuration
    @Column(name = "enable_audit_logging")
    private Boolean enableAuditLogging;

    @Column(name = "enable_metrics")
    private Boolean enableMetrics;

    @Column(name = "enable_distributed_tracing")
    private Boolean enableDistributedTracing;

    @Column(name = "log_retention_days")
    private Integer logRetentionDays;

    // Integration Configuration
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "tenant_integrations", 
                    joinColumns = @JoinColumn(name = "tenant_config_id"))
    @MapKeyColumn(name = "integration_name")
    @Column(name = "integration_config", columnDefinition = "TEXT")
    private Map<String, String> integrations;

    // API Configuration
    @Column(name = "api_version", length = 10)
    private String apiVersion;

    @Column(name = "enable_api_versioning")
    private Boolean enableApiVersioning;

    @Column(name = "default_page_size")
    private Integer defaultPageSize;

    @Column(name = "max_page_size")
    private Integer maxPageSize;

    // Branding Configuration
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "primary_color", length = 7)
    private String primaryColor;

    @Column(name = "secondary_color", length = 7)
    private String secondaryColor;

    @Column(name = "custom_css", columnDefinition = "TEXT")
    private String customCss;

    // Notification Configuration
    @Column(name = "enable_email_notifications")
    private Boolean enableEmailNotifications;

    @Column(name = "enable_sms_notifications")
    private Boolean enableSmsNotifications;

    @Column(name = "enable_push_notifications")
    private Boolean enablePushNotifications;

    @Column(name = "notification_sender_email", length = 100)
    private String notificationSenderEmail;

    // Data Management Configuration
    @Column(name = "data_retention_days")
    private Integer dataRetentionDays;

    @Column(name = "enable_data_encryption")
    private Boolean enableDataEncryption;

    @Column(name = "backup_frequency", length = 20)
    private String backupFrequency;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Password Policy Configuration.
     */
    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PasswordPolicy {
        private Integer minLength;
        private Boolean requireUppercase;
        private Boolean requireLowercase;
        private Boolean requireNumbers;
        private Boolean requireSpecialChars;
        private Integer maxAge; // days
        private Integer preventReuse; // number of previous passwords to prevent reuse
    }

    /**
     * Rate Limiting Configuration.
     */
    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLimitingConfig {
        private Boolean enabled;
        private Integer defaultRequestsPerMinute;
        private Integer burstCapacity;
        private Boolean enableIpRateLimiting;
        private Boolean enableUserRateLimiting;
    }
}
