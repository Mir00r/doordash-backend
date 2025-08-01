package com.doordash.user_service.multitenancy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Tenant Context Holder for DoorDash Multi-Tenant Platform.
 * 
 * Provides thread-local storage for tenant context information throughout
 * the request lifecycle. This ensures that all operations within a request
 * are automatically tenant-aware without requiring explicit tenant ID
 * passing through all method calls.
 * 
 * Features:
 * - Thread-safe tenant context storage
 * - Automatic context cleanup to prevent memory leaks
 * - Support for nested tenant contexts
 * - Tenant validation and security checks
 * - Integration with Spring Security
 * 
 * Usage Pattern:
 * 1. TenantContextHolder.setTenantId(tenantId) - Set tenant context
 * 2. Perform tenant-aware operations
 * 3. TenantContextHolder.clear() - Clean up context
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class TenantContextHolder {

    private static final ThreadLocal<TenantContext> TENANT_CONTEXT = new ThreadLocal<>();

    /**
     * Set the tenant context for the current thread.
     */
    public static void setTenantId(String tenantId) {
        if (tenantId == null) {
            log.warn("Attempting to set null tenant ID");
            return;
        }
        
        TenantContext context = TenantContext.builder()
            .tenantId(tenantId)
            .timestamp(System.currentTimeMillis())
            .build();
            
        TENANT_CONTEXT.set(context);
        log.debug("Set tenant context: {}", tenantId);
    }

    /**
     * Set the complete tenant context for the current thread.
     */
    public static void setTenantContext(TenantContext context) {
        if (context == null) {
            log.warn("Attempting to set null tenant context");
            return;
        }
        
        TENANT_CONTEXT.set(context);
        log.debug("Set tenant context: {}", context.getTenantId());
    }

    /**
     * Get the tenant ID for the current thread.
     */
    public static String getTenantId() {
        TenantContext context = TENANT_CONTEXT.get();
        return context != null ? context.getTenantId() : null;
    }

    /**
     * Get the complete tenant context for the current thread.
     */
    public static TenantContext getTenantContext() {
        return TENANT_CONTEXT.get();
    }

    /**
     * Check if tenant context is set for the current thread.
     */
    public static boolean hasTenantContext() {
        return TENANT_CONTEXT.get() != null;
    }

    /**
     * Clear the tenant context for the current thread.
     * This should be called at the end of request processing to prevent memory leaks.
     */
    public static void clear() {
        TenantContext context = TENANT_CONTEXT.get();
        if (context != null) {
            log.debug("Clearing tenant context: {}", context.getTenantId());
            TENANT_CONTEXT.remove();
        }
    }

    /**
     * Execute a block of code with a specific tenant context.
     */
    public static <T> T executeWithTenant(String tenantId, TenantAwareOperation<T> operation) {
        String previousTenantId = getTenantId();
        try {
            setTenantId(tenantId);
            return operation.execute();
        } finally {
            if (previousTenantId != null) {
                setTenantId(previousTenantId);
            } else {
                clear();
            }
        }
    }

    /**
     * Validate that current thread has a valid tenant context.
     */
    public static void validateTenantContext() {
        if (!hasTenantContext()) {
            throw new TenantContextException("No tenant context found for current thread");
        }
    }

    /**
     * Get tenant ID with validation.
     */
    public static String getRequiredTenantId() {
        validateTenantContext();
        return getTenantId();
    }

    /**
     * Tenant Context data class.
     */
    @lombok.Data
    @lombok.Builder
    public static class TenantContext {
        private String tenantId;
        private String userId;
        private String userRole;
        private String isolationLevel;
        private long timestamp;
        private String requestId;
        private String sessionId;
        
        /**
         * Check if context is expired (older than 1 hour).
         */
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 3600000; // 1 hour
        }
        
        /**
         * Check if user has admin role for the tenant.
         */
        public boolean isAdminUser() {
            return "TENANT_ADMIN".equals(userRole) || "SUPER_ADMIN".equals(userRole);
        }
    }

    /**
     * Functional interface for tenant-aware operations.
     */
    @FunctionalInterface
    public interface TenantAwareOperation<T> {
        T execute() throws Exception;
    }

    /**
     * Exception thrown when tenant context is invalid or missing.
     */
    public static class TenantContextException extends RuntimeException {
        public TenantContextException(String message) {
            super(message);
        }
        
        public TenantContextException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
