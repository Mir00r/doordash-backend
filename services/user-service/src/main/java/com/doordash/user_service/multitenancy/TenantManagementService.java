package com.doordash.user_service.multitenancy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Multi-Tenant Management Service for DoorDash Platform.
 * 
 * Provides comprehensive tenant management capabilities including:
 * - Tenant lifecycle management (creation, activation, suspension, deletion)
 * - Tenant configuration and feature flag management
 * - Tenant isolation and data segregation
 * - Tenant-specific rate limiting and quota management
 * - Tenant hierarchy and organization management
 * - Tenant billing and subscription management
 * - Audit logging and compliance tracking
 * 
 * Multi-tenancy patterns supported:
 * - Schema-per-tenant: Complete database isolation
 * - Table-per-tenant: Shared database with tenant-specific tables
 * - Row-level-security: Shared tables with tenant-based filtering
 * - Hybrid approach: Critical data isolated, shared data partitioned
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TenantManagementService {

    private final TenantRepository tenantRepository;
    private final TenantConfigurationRepository tenantConfigurationRepository;
    private final TenantAuditRepository tenantAuditRepository;

    /**
     * Create a new tenant with default configuration.
     */
    public Tenant createTenant(CreateTenantRequest request) {
        log.info("Creating new tenant: {}", request.getName());
        
        // Validate tenant creation request
        validateCreateTenantRequest(request);
        
        // Create tenant entity
        Tenant tenant = Tenant.builder()
            .id(UUID.randomUUID().toString())
            .name(request.getName())
            .displayName(request.getDisplayName())
            .domain(request.getDomain())
            .subscriptionPlan(request.getSubscriptionPlan())
            .status(TenantStatus.PENDING)
            .isolationLevel(request.getIsolationLevel())
            .maxUsers(request.getMaxUsers())
            .maxApiCallsPerHour(request.getMaxApiCallsPerHour())
            .enabledFeatures(request.getEnabledFeatures())
            .parentTenantId(request.getParentTenantId())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // Save tenant
        tenant = tenantRepository.save(tenant);
        
        // Create default tenant configuration
        createDefaultTenantConfiguration(tenant);
        
        // Log tenant creation
        auditTenantAction(tenant.getId(), "TENANT_CREATED", 
            "Tenant created with plan: " + request.getSubscriptionPlan());
        
        log.info("Successfully created tenant: {} with ID: {}", tenant.getName(), tenant.getId());
        return tenant;
    }

    /**
     * Activate a tenant and enable full functionality.
     */
    public Tenant activateTenant(String tenantId, String activatedByUserId) {
        log.info("Activating tenant: {}", tenantId);
        
        Tenant tenant = getTenantById(tenantId);
        
        if (tenant.getStatus() == TenantStatus.ACTIVE) {
            throw new TenantException("Tenant is already active: " + tenantId);
        }
        
        // Validate tenant can be activated
        validateTenantActivation(tenant);
        
        // Update tenant status
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setActivatedAt(LocalDateTime.now());
        tenant.setActivatedByUserId(activatedByUserId);
        tenant.setUpdatedAt(LocalDateTime.now());
        
        tenant = tenantRepository.save(tenant);
        
        // Initialize tenant resources
        initializeTenantResources(tenant);
        
        // Log activation
        auditTenantAction(tenantId, "TENANT_ACTIVATED", 
            "Tenant activated by user: " + activatedByUserId);
        
        log.info("Successfully activated tenant: {}", tenantId);
        return tenant;
    }

    /**
     * Suspend a tenant and disable functionality.
     */
    public Tenant suspendTenant(String tenantId, String reason, String suspendedByUserId) {
        log.info("Suspending tenant: {} for reason: {}", tenantId, reason);
        
        Tenant tenant = getTenantById(tenantId);
        
        if (tenant.getStatus() == TenantStatus.SUSPENDED) {
            throw new TenantException("Tenant is already suspended: " + tenantId);
        }
        
        // Update tenant status
        tenant.setStatus(TenantStatus.SUSPENDED);
        tenant.setSuspensionReason(reason);
        tenant.setSuspendedAt(LocalDateTime.now());
        tenant.setSuspendedByUserId(suspendedByUserId);
        tenant.setUpdatedAt(LocalDateTime.now());
        
        tenant = tenantRepository.save(tenant);
        
        // Cleanup active tenant sessions
        cleanupTenantSessions(tenantId);
        
        // Log suspension
        auditTenantAction(tenantId, "TENANT_SUSPENDED", 
            "Tenant suspended by user: " + suspendedByUserId + ", reason: " + reason);
        
        log.info("Successfully suspended tenant: {}", tenantId);
        return tenant;
    }

    /**
     * Update tenant configuration.
     */
    public Tenant updateTenantConfiguration(String tenantId, UpdateTenantRequest request) {
        log.info("Updating tenant configuration: {}", tenantId);
        
        Tenant tenant = getTenantById(tenantId);
        
        // Update tenant properties
        if (request.getDisplayName() != null) {
            tenant.setDisplayName(request.getDisplayName());
        }
        
        if (request.getMaxUsers() != null) {
            tenant.setMaxUsers(request.getMaxUsers());
        }
        
        if (request.getMaxApiCallsPerHour() != null) {
            tenant.setMaxApiCallsPerHour(request.getMaxApiCallsPerHour());
        }
        
        if (request.getEnabledFeatures() != null) {
            tenant.setEnabledFeatures(request.getEnabledFeatures());
        }
        
        if (request.getSubscriptionPlan() != null) {
            updateTenantSubscription(tenant, request.getSubscriptionPlan());
        }
        
        tenant.setUpdatedAt(LocalDateTime.now());
        tenant = tenantRepository.save(tenant);
        
        // Log configuration update
        auditTenantAction(tenantId, "TENANT_UPDATED", 
            "Tenant configuration updated");
        
        log.info("Successfully updated tenant configuration: {}", tenantId);
        return tenant;
    }

    /**
     * Get tenant by ID with caching.
     */
    @Cacheable(value = "tenants", key = "#tenantId")
    public Tenant getTenantById(String tenantId) {
        return tenantRepository.findById(tenantId)
            .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantId));
    }

    /**
     * Get tenant by domain name.
     */
    @Cacheable(value = "tenants", key = "'domain:' + #domain")
    public Optional<Tenant> getTenantByDomain(String domain) {
        return tenantRepository.findByDomain(domain);
    }

    /**
     * Get all active tenants.
     */
    public List<Tenant> getActiveTenants() {
        return tenantRepository.findByStatus(TenantStatus.ACTIVE);
    }

    /**
     * Get tenant configuration.
     */
    @Cacheable(value = "tenant-configs", key = "#tenantId")
    public TenantConfiguration getTenantConfiguration(String tenantId) {
        return tenantConfigurationRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new TenantException("Tenant configuration not found: " + tenantId));
    }

    /**
     * Update tenant configuration.
     */
    public TenantConfiguration updateTenantConfiguration(String tenantId, 
                                                       TenantConfiguration configuration) {
        configuration.setTenantId(tenantId);
        configuration.setUpdatedAt(LocalDateTime.now());
        
        TenantConfiguration saved = tenantConfigurationRepository.save(configuration);
        
        auditTenantAction(tenantId, "CONFIGURATION_UPDATED", 
            "Tenant configuration updated");
        
        return saved;
    }

    /**
     * Check if a feature is enabled for a tenant.
     */
    public boolean isFeatureEnabled(String tenantId, String featureName) {
        try {
            Tenant tenant = getTenantById(tenantId);
            return tenant.getEnabledFeatures().contains(featureName);
        } catch (Exception e) {
            log.error("Error checking feature flag for tenant: {}", tenantId, e);
            return false; // Fail safe - disable feature if unable to check
        }
    }

    /**
     * Get tenant hierarchy (parent and children).
     */
    public TenantHierarchy getTenantHierarchy(String tenantId) {
        Tenant tenant = getTenantById(tenantId);
        
        // Get parent tenant
        Tenant parent = null;
        if (tenant.getParentTenantId() != null) {
            parent = getTenantById(tenant.getParentTenantId());
        }
        
        // Get child tenants
        List<Tenant> children = tenantRepository.findByParentTenantId(tenantId);
        
        return TenantHierarchy.builder()
            .tenant(tenant)
            .parent(parent)
            .children(children)
            .build();
    }

    /**
     * Get tenant metrics and usage statistics.
     */
    public TenantMetrics getTenantMetrics(String tenantId) {
        Tenant tenant = getTenantById(tenantId);
        
        // Calculate various metrics
        long activeUsers = tenantRepository.countActiveUsersByTenantId(tenantId);
        long apiCallsLastHour = tenantRepository.countApiCallsLastHour(tenantId);
        long apiCallsToday = tenantRepository.countApiCallsToday(tenantId);
        double storageUsedMB = tenantRepository.calculateStorageUsage(tenantId);
        
        return TenantMetrics.builder()
            .tenantId(tenantId)
            .activeUsers(activeUsers)
            .maxUsers(tenant.getMaxUsers())
            .apiCallsLastHour(apiCallsLastHour)
            .maxApiCallsPerHour(tenant.getMaxApiCallsPerHour())
            .apiCallsToday(apiCallsToday)
            .storageUsedMB(storageUsedMB)
            .subscriptionPlan(tenant.getSubscriptionPlan())
            .status(tenant.getStatus())
            .createdAt(tenant.getCreatedAt())
            .lastActiveAt(tenant.getLastActiveAt())
            .build();
    }

    // Private helper methods

    private void validateCreateTenantRequest(CreateTenantRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new TenantException("Tenant name is required");
        }
        
        if (request.getDomain() != null && tenantRepository.existsByDomain(request.getDomain())) {
            throw new TenantException("Domain already exists: " + request.getDomain());
        }
        
        if (request.getIsolationLevel() == null) {
            throw new TenantException("Isolation level is required");
        }
    }

    private void validateTenantActivation(Tenant tenant) {
        if (tenant.getStatus() == TenantStatus.DELETED) {
            throw new TenantException("Cannot activate deleted tenant: " + tenant.getId());
        }
        
        // Add additional validation logic as needed
    }

    private void createDefaultTenantConfiguration(Tenant tenant) {
        TenantConfiguration config = TenantConfiguration.builder()
            .tenantId(tenant.getId())
            .timeZone("UTC")
            .dateFormat("yyyy-MM-dd")
            .currency("USD")
            .language("en")
            .enableAuditLogging(true)
            .enableMetrics(true)
            .enableDistributedTracing(true)
            .sessionTimeoutMinutes(30)
            .passwordPolicy(createDefaultPasswordPolicy())
            .rateLimiting(createDefaultRateLimitingConfig())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        tenantConfigurationRepository.save(config);
    }

    private TenantConfiguration.PasswordPolicy createDefaultPasswordPolicy() {
        return TenantConfiguration.PasswordPolicy.builder()
            .minLength(8)
            .requireUppercase(true)
            .requireLowercase(true)
            .requireNumbers(true)
            .requireSpecialChars(true)
            .maxAge(90)
            .preventReuse(5)
            .build();
    }

    private TenantConfiguration.RateLimitingConfig createDefaultRateLimitingConfig() {
        return TenantConfiguration.RateLimitingConfig.builder()
            .enabled(true)
            .defaultRequestsPerMinute(100)
            .burstCapacity(150)
            .enableIpRateLimiting(true)
            .enableUserRateLimiting(true)
            .build();
    }

    private void updateTenantSubscription(Tenant tenant, String newSubscriptionPlan) {
        String oldPlan = tenant.getSubscriptionPlan();
        tenant.setSubscriptionPlan(newSubscriptionPlan);
        
        // Update tenant limits based on new subscription plan
        updateTenantLimitsForPlan(tenant, newSubscriptionPlan);
        
        auditTenantAction(tenant.getId(), "SUBSCRIPTION_CHANGED", 
            "Subscription changed from " + oldPlan + " to " + newSubscriptionPlan);
    }

    private void updateTenantLimitsForPlan(Tenant tenant, String plan) {
        switch (plan.toLowerCase()) {
            case "free" -> {
                tenant.setMaxUsers(10L);
                tenant.setMaxApiCallsPerHour(1000L);
            }
            case "basic" -> {
                tenant.setMaxUsers(100L);
                tenant.setMaxApiCallsPerHour(10000L);
            }
            case "premium" -> {
                tenant.setMaxUsers(1000L);
                tenant.setMaxApiCallsPerHour(100000L);
            }
            case "enterprise" -> {
                tenant.setMaxUsers(10000L);
                tenant.setMaxApiCallsPerHour(1000000L);
            }
        }
    }

    private void initializeTenantResources(Tenant tenant) {
        // Initialize tenant-specific resources
        // This could include creating database schemas, S3 buckets, etc.
        log.info("Initializing resources for tenant: {}", tenant.getId());
    }

    private void cleanupTenantSessions(String tenantId) {
        // Cleanup active sessions for suspended tenant
        log.info("Cleaning up sessions for suspended tenant: {}", tenantId);
    }

    private void auditTenantAction(String tenantId, String action, String details) {
        TenantAudit audit = TenantAudit.builder()
            .id(UUID.randomUUID().toString())
            .tenantId(tenantId)
            .action(action)
            .details(details)
            .timestamp(LocalDateTime.now())
            .build();
        
        tenantAuditRepository.save(audit);
    }
}
