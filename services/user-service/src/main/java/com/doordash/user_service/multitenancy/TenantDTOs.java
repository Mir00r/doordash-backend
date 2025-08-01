package com.doordash.user_service.multitenancy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Data Transfer Objects for Multi-Tenancy System.
 * 
 * Contains all DTOs used for tenant management operations including:
 * - CreateTenantRequest: Request to create new tenant
 * - UpdateTenantRequest: Request to update tenant information
 * - TenantHierarchy: Represents tenant parent-child relationships
 * - TenantMetrics: Tenant usage and performance metrics
 * - TenantAudit: Audit log entry for tenant operations
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
public class TenantDTOs {

    /**
     * Request DTO for creating a new tenant.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateTenantRequest {
        private String name;
        private String displayName;
        private String domain;
        private String subscriptionPlan;
        private IsolationLevel isolationLevel;
        private Long maxUsers;
        private Long maxApiCallsPerHour;
        private Set<String> enabledFeatures;
        private String parentTenantId;
        private String billingEmail;
        private String technicalContactEmail;
        
        // Validation method
        public void validate() {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Tenant name is required");
            }
            if (subscriptionPlan == null || subscriptionPlan.trim().isEmpty()) {
                throw new IllegalArgumentException("Subscription plan is required");
            }
            if (isolationLevel == null) {
                throw new IllegalArgumentException("Isolation level is required");
            }
        }
    }

    /**
     * Request DTO for updating tenant information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateTenantRequest {
        private String displayName;
        private Long maxUsers;
        private Long maxApiCallsPerHour;
        private Set<String> enabledFeatures;
        private String subscriptionPlan;
        private String billingEmail;
        private String technicalContactEmail;
    }

    /**
     * DTO representing tenant hierarchy relationships.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantHierarchy {
        private Tenant tenant;
        private Tenant parent;
        private List<Tenant> children;
        
        /**
         * Check if this tenant is a root tenant (no parent).
         */
        public boolean isRoot() {
            return parent == null;
        }
        
        /**
         * Check if this tenant has child tenants.
         */
        public boolean hasChildren() {
            return children != null && !children.isEmpty();
        }
        
        /**
         * Get the depth level in the hierarchy (0 for root).
         */
        public int getDepthLevel() {
            // This would need to be calculated by traversing up the hierarchy
            // For now, returning simple calculation
            return parent == null ? 0 : 1;
        }
    }

    /**
     * DTO for tenant usage metrics and statistics.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantMetrics {
        private String tenantId;
        private long activeUsers;
        private long maxUsers;
        private long apiCallsLastHour;
        private long maxApiCallsPerHour;
        private long apiCallsToday;
        private double storageUsedMB;
        private String subscriptionPlan;
        private TenantStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime lastActiveAt;
        private double userUtilizationPercent;
        private double apiUtilizationPercent;
        
        /**
         * Calculate user utilization percentage.
         */
        public double calculateUserUtilization() {
            if (maxUsers == 0) return 0.0;
            return (double) activeUsers / maxUsers * 100.0;
        }
        
        /**
         * Calculate API utilization percentage.
         */
        public double calculateApiUtilization() {
            if (maxApiCallsPerHour == 0) return 0.0;
            return (double) apiCallsLastHour / maxApiCallsPerHour * 100.0;
        }
        
        /**
         * Check if tenant is approaching user limit.
         */
        public boolean isApproachingUserLimit() {
            return calculateUserUtilization() > 85.0;
        }
        
        /**
         * Check if tenant is approaching API limit.
         */
        public boolean isApproachingApiLimit() {
            return calculateApiUtilization() > 85.0;
        }
    }

    /**
     * DTO for tenant audit log entries.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantAudit {
        private String id;
        private String tenantId;
        private String action;
        private String details;
        private String performedByUserId;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime timestamp;
        
        /**
         * Create audit entry for tenant creation.
         */
        public static TenantAudit createTenantCreated(String tenantId, String userId, String details) {
            return TenantAudit.builder()
                .tenantId(tenantId)
                .action("TENANT_CREATED")
                .details(details)
                .performedByUserId(userId)
                .timestamp(LocalDateTime.now())
                .build();
        }
        
        /**
         * Create audit entry for tenant activation.
         */
        public static TenantAudit createTenantActivated(String tenantId, String userId) {
            return TenantAudit.builder()
                .tenantId(tenantId)
                .action("TENANT_ACTIVATED")
                .details("Tenant activated")
                .performedByUserId(userId)
                .timestamp(LocalDateTime.now())
                .build();
        }
        
        /**
         * Create audit entry for tenant suspension.
         */
        public static TenantAudit createTenantSuspended(String tenantId, String userId, String reason) {
            return TenantAudit.builder()
                .tenantId(tenantId)
                .action("TENANT_SUSPENDED")
                .details("Tenant suspended: " + reason)
                .performedByUserId(userId)
                .timestamp(LocalDateTime.now())
                .build();
        }
    }

    /**
     * DTO for tenant feature configuration.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantFeatureConfig {
        private String tenantId;
        private String featureName;
        private boolean enabled;
        private String configuration; // JSON configuration for the feature
        private LocalDateTime enabledAt;
        private String enabledByUserId;
        
        /**
         * Check if feature is currently enabled.
         */
        public boolean isCurrentlyEnabled() {
            return enabled;
        }
    }

    /**
     * DTO for tenant subscription information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantSubscription {
        private String tenantId;
        private String planName;
        private String planType; // MONTHLY, YEARLY, ENTERPRISE
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private boolean autoRenew;
        private String billingStatus; // ACTIVE, SUSPENDED, CANCELLED
        private double monthlyPrice;
        private String currency;
        
        /**
         * Check if subscription is currently active.
         */
        public boolean isActive() {
            LocalDateTime now = LocalDateTime.now();
            return startDate.isBefore(now) && 
                   (endDate == null || endDate.isAfter(now)) &&
                   "ACTIVE".equals(billingStatus);
        }
        
        /**
         * Check if subscription is expiring soon (within 30 days).
         */
        public boolean isExpiringSoon() {
            if (endDate == null) return false;
            return endDate.isBefore(LocalDateTime.now().plusDays(30));
        }
    }

    /**
     * Response DTO for tenant list operations.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantListResponse {
        private List<TenantSummary> tenants;
        private int totalCount;
        private int pageNumber;
        private int pageSize;
        private boolean hasMore;
        
        /**
         * Summary information for tenant list.
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TenantSummary {
            private String id;
            private String name;
            private String displayName;
            private String domain;
            private TenantStatus status;
            private String subscriptionPlan;
            private long activeUsers;
            private LocalDateTime createdAt;
            private LocalDateTime lastActiveAt;
        }
    }
}

/**
 * Custom exceptions for tenant management.
 */
class TenantException extends RuntimeException {
    public TenantException(String message) {
        super(message);
    }
    
    public TenantException(String message, Throwable cause) {
        super(message, cause);
    }
}

class TenantNotFoundException extends TenantException {
    public TenantNotFoundException(String message) {
        super(message);
    }
}

class TenantAlreadyExistsException extends TenantException {
    public TenantAlreadyExistsException(String message) {
        super(message);
    }
}

class TenantInvalidStateException extends TenantException {
    public TenantInvalidStateException(String message) {
        super(message);
    }
}
