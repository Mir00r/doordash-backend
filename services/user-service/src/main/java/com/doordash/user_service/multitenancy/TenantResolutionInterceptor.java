package com.doordash.user_service.multitenancy;

import com.doordash.user_service.observability.security.SecurityMetricsService;
import io.jaeger.Tracer;
import io.opentracing.Span;
import io.opentracing.tag.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Tenant Resolution HTTP Interceptor for DoorDash Multi-Tenant Platform.
 * 
 * Automatically resolves and sets tenant context for incoming HTTP requests.
 * Supports multiple tenant resolution strategies:
 * 
 * 1. JWT Claims: Extract tenant ID from authenticated user's JWT token
 * 2. Subdomain: Resolve tenant from subdomain (e.g., acme.doordash.com)
 * 3. Header: Extract tenant ID from X-Tenant-ID header
 * 4. Query Parameter: Extract tenant ID from tenant_id query parameter
 * 5. Path Variable: Extract tenant ID from URL path (e.g., /api/tenants/{tenantId}/...)
 * 
 * The interceptor also:
 * - Validates tenant status and access permissions
 * - Sets up distributed tracing with tenant context
 * - Records tenant-specific metrics
 * - Enforces tenant isolation security policies
 * - Handles tenant context cleanup
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantResolutionInterceptor implements HandlerInterceptor {

    private final TenantManagementService tenantManagementService;
    private final SecurityMetricsService securityMetricsService;
    private final Tracer tracer;

    // Headers and parameters for tenant resolution
    private static final String TENANT_ID_HEADER = "X-Tenant-ID";
    private static final String TENANT_ID_PARAM = "tenant_id";
    private static final String TENANT_DOMAIN_HEADER = "X-Tenant-Domain";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, 
                           @NonNull HttpServletResponse response, 
                           @NonNull Object handler) throws Exception {
        
        Span span = tracer.nextSpan()
            .withTag("component", "tenant-resolution-interceptor")
            .withTag("http.method", request.getMethod())
            .withTag("http.url", request.getRequestURI())
            .start();

        try {
            // Resolve tenant ID using multiple strategies
            String tenantId = resolveTenantId(request);
            
            if (tenantId != null) {
                // Validate tenant and set context
                boolean contextSet = setTenantContext(tenantId, request, span);
                
                if (!contextSet) {
                    // Tenant validation failed
                    span.setTag(Tags.ERROR, true);
                    span.setTag("tenant.validation.failed", true);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write(buildTenantErrorResponse("Invalid or inactive tenant"));
                    return false;
                }
                
                // Set response headers with tenant information
                response.setHeader("X-Tenant-ID", tenantId);
                span.setTag("tenant.id", tenantId);
                
                log.debug("Successfully set tenant context: {} for request: {}", 
                    tenantId, request.getRequestURI());
                    
            } else {
                // No tenant context - this might be okay for public endpoints
                log.debug("No tenant context resolved for request: {}", request.getRequestURI());
                span.setTag("tenant.resolution", "none");
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Error in tenant resolution interceptor", e);
            span.setTag(Tags.ERROR, true);
            span.setTag("error.message", e.getMessage());
            
            // Fail open - allow request to proceed without tenant context for public endpoints
            return true;
            
        } finally {
            span.finish();
        }
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, 
                              @NonNull HttpServletResponse response, 
                              @NonNull Object handler, 
                              Exception ex) throws Exception {
        
        // Clean up tenant context to prevent memory leaks
        try {
            if (TenantContextHolder.hasTenantContext()) {
                String tenantId = TenantContextHolder.getTenantId();
                log.debug("Cleaning up tenant context: {}", tenantId);
                TenantContextHolder.clear();
            }
        } catch (Exception e) {
            log.warn("Error cleaning up tenant context", e);
        }
    }

    /**
     * Resolve tenant ID using multiple strategies.
     */
    private String resolveTenantId(HttpServletRequest request) {
        // Strategy 1: Extract from JWT claims (authenticated users)
        String tenantId = resolveTenantFromJwt();
        if (tenantId != null) {
            log.debug("Resolved tenant from JWT: {}", tenantId);
            return tenantId;
        }

        // Strategy 2: Extract from subdomain
        tenantId = resolveTenantFromSubdomain(request);
        if (tenantId != null) {
            log.debug("Resolved tenant from subdomain: {}", tenantId);
            return tenantId;
        }

        // Strategy 3: Extract from header
        tenantId = request.getHeader(TENANT_ID_HEADER);
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            log.debug("Resolved tenant from header: {}", tenantId);
            return tenantId;
        }

        // Strategy 4: Extract from query parameter
        tenantId = request.getParameter(TENANT_ID_PARAM);
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            log.debug("Resolved tenant from query parameter: {}", tenantId);
            return tenantId;
        }

        // Strategy 5: Extract from path variable
        tenantId = resolveTenantFromPath(request);
        if (tenantId != null) {
            log.debug("Resolved tenant from path: {}", tenantId);
            return tenantId;
        }

        // Strategy 6: Extract from domain header
        String domain = request.getHeader(TENANT_DOMAIN_HEADER);
        if (domain != null && !domain.trim().isEmpty()) {
            tenantId = resolveTenantFromDomain(domain);
            if (tenantId != null) {
                log.debug("Resolved tenant from domain: {}", tenantId);
                return tenantId;
            }
        }

        return null;
    }

    /**
     * Resolve tenant ID from JWT claims.
     */
    private String resolveTenantFromJwt() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return Optional.ofNullable(authentication.getDetails())
                    .map(details -> {
                        if (details instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
                            return jwt.getClaimAsString("tenant_id");
                        }
                        return null;
                    })
                    .orElse(null);
            }
        } catch (Exception e) {
            log.debug("Could not resolve tenant from JWT", e);
        }
        return null;
    }

    /**
     * Resolve tenant ID from subdomain.
     */
    private String resolveTenantFromSubdomain(HttpServletRequest request) {
        try {
            String serverName = request.getServerName();
            if (serverName != null && serverName.contains(".")) {
                String[] parts = serverName.split("\\.");
                if (parts.length >= 3) { // e.g., tenant.doordash.com
                    String subdomain = parts[0];
                    if (!"www".equals(subdomain) && !"api".equals(subdomain)) {
                        return resolveTenantFromDomain(subdomain);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not resolve tenant from subdomain", e);
        }
        return null;
    }

    /**
     * Resolve tenant ID from URL path.
     */
    private String resolveTenantFromPath(HttpServletRequest request) {
        try {
            String path = request.getRequestURI();
            // Pattern: /api/tenants/{tenantId}/...
            if (path.startsWith("/api/tenants/")) {
                String[] pathParts = path.split("/");
                if (pathParts.length >= 4) {
                    return pathParts[3]; // tenant ID is at index 3
                }
            }
            
            // Pattern: /api/v1/tenant/{tenantId}/...
            if (path.contains("/tenant/")) {
                String[] pathParts = path.split("/tenant/");
                if (pathParts.length >= 2) {
                    String tenantPart = pathParts[1];
                    int nextSlash = tenantPart.indexOf('/');
                    return nextSlash > 0 ? tenantPart.substring(0, nextSlash) : tenantPart;
                }
            }
        } catch (Exception e) {
            log.debug("Could not resolve tenant from path", e);
        }
        return null;
    }

    /**
     * Resolve tenant ID from domain name.
     */
    private String resolveTenantFromDomain(String domain) {
        try {
            Optional<Tenant> tenant = tenantManagementService.getTenantByDomain(domain);
            return tenant.map(Tenant::getId).orElse(null);
        } catch (Exception e) {
            log.debug("Could not resolve tenant from domain: {}", domain, e);
            return null;
        }
    }

    /**
     * Validate tenant and set context.
     */
    private boolean setTenantContext(String tenantId, HttpServletRequest request, Span span) {
        try {
            // Get tenant information
            Tenant tenant = tenantManagementService.getTenantById(tenantId);
            
            // Validate tenant status
            if (!tenant.isActive()) {
                log.warn("Attempted access to inactive tenant: {}", tenantId);
                securityMetricsService.recordTenantAccessAttempt(tenantId, false, "INACTIVE_TENANT");
                span.setTag("tenant.status", tenant.getStatus().toString());
                return false;
            }
            
            // Build and set tenant context
            TenantContextHolder.TenantContext context = buildTenantContext(tenant, request);
            TenantContextHolder.setTenantContext(context);
            
            // Record successful tenant access
            securityMetricsService.recordTenantAccessAttempt(tenantId, true, "SUCCESS");
            span.setTag("tenant.status", "ACTIVE");
            span.setTag("tenant.isolation_level", tenant.getIsolationLevel().toString());
            
            return true;
            
        } catch (TenantNotFoundException e) {
            log.warn("Invalid tenant ID: {}", tenantId);
            securityMetricsService.recordTenantAccessAttempt(tenantId, false, "TENANT_NOT_FOUND");
            span.setTag("tenant.error", "NOT_FOUND");
            return false;
        } catch (Exception e) {
            log.error("Error validating tenant: {}", tenantId, e);
            securityMetricsService.recordTenantAccessAttempt(tenantId, false, "VALIDATION_ERROR");
            span.setTag("tenant.error", "VALIDATION_FAILED");
            return false;
        }
    }

    /**
     * Build tenant context from request and tenant information.
     */
    private TenantContextHolder.TenantContext buildTenantContext(Tenant tenant, HttpServletRequest request) {
        TenantContextHolder.TenantContext.TenantContextBuilder builder = 
            TenantContextHolder.TenantContext.builder()
                .tenantId(tenant.getId())
                .isolationLevel(tenant.getIsolationLevel().toString())
                .timestamp(System.currentTimeMillis())
                .requestId(request.getHeader("X-Request-ID"));

        // Extract user information from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            builder.userId(authentication.getName());
            
            // Extract user role from JWT claims
            Optional.ofNullable(authentication.getDetails())
                .map(details -> {
                    if (details instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
                        return jwt.getClaimAsString("role");
                    }
                    return null;
                })
                .ifPresent(builder::userRole);
        }

        // Extract session ID from headers or cookies
        String sessionId = request.getHeader("X-Session-ID");
        if (sessionId == null) {
            // Try to get from cookies
            if (request.getCookies() != null) {
                for (var cookie : request.getCookies()) {
                    if ("JSESSIONID".equals(cookie.getName())) {
                        sessionId = cookie.getValue();
                        break;
                    }
                }
            }
        }
        builder.sessionId(sessionId);

        return builder.build();
    }

    /**
     * Build JSON error response for tenant validation failures.
     */
    private String buildTenantErrorResponse(String message) {
        return String.format("""
            {
                "error": "tenant_access_denied",
                "message": "%s",
                "timestamp": %d
            }
            """, message, System.currentTimeMillis());
    }
}
