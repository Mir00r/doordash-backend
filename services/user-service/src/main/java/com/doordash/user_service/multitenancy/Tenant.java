package com.doordash.user_service.multitenancy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Tenant Entity for Multi-Tenant DoorDash Platform.
 * 
 * Represents a tenant (organization/company) in the multi-tenant system.
 * Each tenant has its own isolated environment with specific configurations,
 * limits, and access controls.
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "tenants", indexes = {
    @Index(name = "idx_tenant_domain", columnList = "domain", unique = true),
    @Index(name = "idx_tenant_status", columnList = "status"),
    @Index(name = "idx_tenant_parent", columnList = "parent_tenant_id"),
    @Index(name = "idx_tenant_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "domain", unique = true, length = 100)
    private String domain;

    @Column(name = "subscription_plan", nullable = false, length = 50)
    private String subscriptionPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TenantStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "isolation_level", nullable = false)
    private IsolationLevel isolationLevel;

    @Column(name = "max_users")
    private Long maxUsers;

    @Column(name = "max_api_calls_per_hour")
    private Long maxApiCallsPerHour;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tenant_features", joinColumns = @JoinColumn(name = "tenant_id"))
    @Column(name = "feature_name")
    private Set<String> enabledFeatures;

    @Column(name = "parent_tenant_id", length = 36)
    private String parentTenantId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "activated_by_user_id", length = 36)
    private String activatedByUserId;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "suspended_by_user_id", length = 36)
    private String suspendedByUserId;

    @Column(name = "suspension_reason", length = 500)
    private String suspensionReason;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "billing_email", length = 100)
    private String billingEmail;

    @Column(name = "technical_contact_email", length = 100)
    private String technicalContactEmail;

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
     * Check if tenant is active and operational.
     */
    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }

    /**
     * Check if tenant is suspended.
     */
    public boolean isSuspended() {
        return status == TenantStatus.SUSPENDED;
    }

    /**
     * Check if a specific feature is enabled for this tenant.
     */
    public boolean hasFeature(String featureName) {
        return enabledFeatures != null && enabledFeatures.contains(featureName);
    }

    /**
     * Check if tenant has hierarchical structure (has parent or children).
     */
    public boolean isHierarchical() {
        return parentTenantId != null;
    }
}

/**
 * Tenant status enumeration.
 */
enum TenantStatus {
    PENDING,    // Tenant created but not yet activated
    ACTIVE,     // Tenant is active and operational
    SUSPENDED,  // Tenant is temporarily suspended
    DELETED     // Tenant is marked for deletion
}

/**
 * Tenant isolation level enumeration.
 */
enum IsolationLevel {
    SCHEMA,     // Complete database schema isolation
    TABLE,      // Table-level isolation within shared schema
    ROW,        // Row-level security within shared tables
    HYBRID      // Mix of isolation levels based on data sensitivity
}
