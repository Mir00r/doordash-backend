package com.doordash.user_service.multitenancy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Tenant entity operations.
 * 
 * Provides data access methods for tenant management including:
 * - Basic CRUD operations
 * - Tenant lookup by various criteria
 * - Tenant hierarchy operations
 * - Tenant metrics and usage queries
 * - Bulk operations for tenant management
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {

    /**
     * Find tenant by domain name.
     */
    Optional<Tenant> findByDomain(String domain);

    /**
     * Check if domain already exists.
     */
    boolean existsByDomain(String domain);

    /**
     * Find all tenants by status.
     */
    List<Tenant> findByStatus(TenantStatus status);

    /**
     * Find all child tenants for a parent tenant.
     */
    List<Tenant> findByParentTenantId(String parentTenantId);

    /**
     * Find tenants by subscription plan.
     */
    List<Tenant> findBySubscriptionPlan(String subscriptionPlan);

    /**
     * Find tenants created within a date range.
     */
    @Query("SELECT t FROM Tenant t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Tenant> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find tenants that were last active within specified period.
     */
    @Query("SELECT t FROM Tenant t WHERE t.lastActiveAt > :since")
    List<Tenant> findActiveTenantsAfter(@Param("since") LocalDateTime since);

    /**
     * Find tenants approaching user limits.
     */
    @Query("""
        SELECT t FROM Tenant t 
        WHERE t.status = 'ACTIVE' 
        AND (SELECT COUNT(u) FROM User u WHERE u.tenantId = t.id AND u.status = 'ACTIVE') 
            >= (t.maxUsers * 0.85)
        """)
    List<Tenant> findTenantsApproachingUserLimits();

    /**
     * Count active users for a tenant.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.status = 'ACTIVE'")
    long countActiveUsersByTenantId(@Param("tenantId") String tenantId);

    /**
     * Count API calls in the last hour for a tenant.
     */
    @Query(value = """
        SELECT COUNT(*) 
        FROM api_usage_log 
        WHERE tenant_id = :tenantId 
        AND created_at > NOW() - INTERVAL 1 HOUR
        """, nativeQuery = true)
    long countApiCallsLastHour(@Param("tenantId") String tenantId);

    /**
     * Count API calls today for a tenant.
     */
    @Query(value = """
        SELECT COUNT(*) 
        FROM api_usage_log 
        WHERE tenant_id = :tenantId 
        AND DATE(created_at) = CURDATE()
        """, nativeQuery = true)
    long countApiCallsToday(@Param("tenantId") String tenantId);

    /**
     * Calculate storage usage for a tenant (in MB).
     */
    @Query(value = """
        SELECT COALESCE(SUM(size_bytes), 0) / 1024 / 1024 
        FROM tenant_storage_usage 
        WHERE tenant_id = :tenantId
        """, nativeQuery = true)
    double calculateStorageUsage(@Param("tenantId") String tenantId);

    /**
     * Find tenants with expired subscriptions.
     */
    @Query("""
        SELECT t FROM Tenant t 
        WHERE t.status = 'ACTIVE' 
        AND EXISTS (
            SELECT 1 FROM TenantSubscription ts 
            WHERE ts.tenantId = t.id 
            AND ts.endDate < :currentDate
        )
        """)
    List<Tenant> findTenantsWithExpiredSubscriptions(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Find tenants by multiple status values.
     */
    List<Tenant> findByStatusIn(List<TenantStatus> statuses);

    /**
     * Find top-level tenants (no parent).
     */
    List<Tenant> findByParentTenantIdIsNull();

    /**
     * Count total tenants by status.
     */
    long countByStatus(TenantStatus status);

    /**
     * Find tenants by name pattern (case-insensitive).
     */
    @Query("SELECT t FROM Tenant t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :namePattern, '%'))")
    List<Tenant> findByNameContainingIgnoreCase(@Param("namePattern") String namePattern);

    /**
     * Update last active timestamp for a tenant.
     */
    @Query("UPDATE Tenant t SET t.lastActiveAt = :timestamp WHERE t.id = :tenantId")
    void updateLastActiveAt(@Param("tenantId") String tenantId, @Param("timestamp") LocalDateTime timestamp);

    /**
     * Bulk update tenant status.
     */
    @Query("UPDATE Tenant t SET t.status = :newStatus WHERE t.id IN :tenantIds")
    int bulkUpdateStatus(@Param("tenantIds") List<String> tenantIds, @Param("newStatus") TenantStatus newStatus);
}

/**
 * Repository interface for TenantConfiguration entity operations.
 */
@Repository
interface TenantConfigurationRepository extends JpaRepository<TenantConfiguration, Long> {

    /**
     * Find configuration by tenant ID.
     */
    Optional<TenantConfiguration> findByTenantId(String tenantId);

    /**
     * Delete configuration by tenant ID.
     */
    void deleteByTenantId(String tenantId);

    /**
     * Check if configuration exists for tenant.
     */
    boolean existsByTenantId(String tenantId);

    /**
     * Find configurations with specific feature enabled.
     */
    @Query("""
        SELECT tc FROM TenantConfiguration tc 
        WHERE tc.enableAuditLogging = true
        """)
    List<TenantConfiguration> findWithAuditLoggingEnabled();

    /**
     * Find configurations with MFA enabled.
     */
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.enableMfa = true")
    List<TenantConfiguration> findWithMfaEnabled();

    /**
     * Find configurations with specific SSO provider.
     */
    List<TenantConfiguration> findBySsoProvider(String ssoProvider);
}

/**
 * Repository interface for TenantAudit entity operations.
 */
@Repository
interface TenantAuditRepository extends JpaRepository<TenantDTOs.TenantAudit, String> {

    /**
     * Find audit entries for a specific tenant.
     */
    List<TenantDTOs.TenantAudit> findByTenantIdOrderByTimestampDesc(String tenantId);

    /**
     * Find audit entries by action type.
     */
    List<TenantDTOs.TenantAudit> findByAction(String action);

    /**
     * Find audit entries within date range.
     */
    @Query("""
        SELECT ta FROM TenantAudit ta 
        WHERE ta.timestamp BETWEEN :startDate AND :endDate 
        ORDER BY ta.timestamp DESC
        """)
    List<TenantDTOs.TenantAudit> findByTimestampBetween(@Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit entries for a specific user.
     */
    List<TenantDTOs.TenantAudit> findByPerformedByUserIdOrderByTimestampDesc(String userId);

    /**
     * Count audit entries for a tenant.
     */
    long countByTenantId(String tenantId);

    /**
     * Delete old audit entries (for data retention).
     */
    @Query("DELETE FROM TenantAudit ta WHERE ta.timestamp < :cutoffDate")
    int deleteOldAuditEntries(@Param("cutoffDate") LocalDateTime cutoffDate);
}
