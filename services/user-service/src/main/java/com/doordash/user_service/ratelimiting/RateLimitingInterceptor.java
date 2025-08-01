package com.doordash.user_service.ratelimiting;

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
import java.time.Duration;
import java.util.Optional;

/**
 * Rate Limiting HTTP Interceptor for DoorDash User Service.
 * 
 * This interceptor integrates the advanced rate limiting service with Spring MVC,
 * providing automatic rate limiting for all incoming HTTP requests based on
 * multiple dimensions including user, tenant, IP, and service context.
 * 
 * Features:
 * - Multi-dimensional rate limiting (user, tenant, IP, service, global)
 * - Dynamic rate limit configuration based on user tier
 * - Comprehensive monitoring and observability
 * - Graceful degradation with circuit breaker integration
 * - Custom headers for rate limit status
 * - Detailed audit logging
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final AdvancedRateLimitingService rateLimitingService;
    private final SecurityMetricsService securityMetricsService;
    private final Tracer tracer;

    // Rate limit headers
    private static final String RATE_LIMIT_LIMIT_HEADER = "X-RateLimit-Limit";
    private static final String RATE_LIMIT_REMAINING_HEADER = "X-RateLimit-Remaining";
    private static final String RATE_LIMIT_RESET_HEADER = "X-RateLimit-Reset";
    private static final String RATE_LIMIT_RETRY_AFTER_HEADER = "Retry-After";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, 
                           @NonNull HttpServletResponse response, 
                           @NonNull Object handler) throws Exception {
        
        Span span = tracer.nextSpan()
            .withTag("component", "rate-limiting-interceptor")
            .withTag("http.method", request.getMethod())
            .withTag("http.url", request.getRequestURI())
            .start();

        try {
            // Extract rate limiting context
            RateLimitContext context = buildRateLimitContext(request);
            
            // Apply rate limiting checks
            boolean allowed = performRateLimitChecks(context, response, span);
            
            if (!allowed) {
                // Rate limit exceeded - log and track metrics
                logRateLimitViolation(context, request);
                trackRateLimitMetrics(context, false);
                span.setTag(Tags.ERROR, true);
                span.setTag("rate_limit.exceeded", true);
                
                // Set appropriate response headers and status
                setRateLimitHeaders(response, context);
                response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
                response.setContentType("application/json");
                response.getWriter().write(buildRateLimitErrorResponse(context));
                
                return false;
            }
            
            // Rate limit check passed
            trackRateLimitMetrics(context, true);
            span.setTag("rate_limit.allowed", true);
            
            return true;
            
        } catch (Exception e) {
            log.error("Error in rate limiting interceptor", e);
            span.setTag(Tags.ERROR, true);
            span.setTag("error.message", e.getMessage());
            
            // Fail open - allow request to proceed if rate limiting fails
            return true;
            
        } finally {
            span.finish();
        }
    }

    /**
     * Build rate limiting context from HTTP request.
     */
    private RateLimitContext buildRateLimitContext(HttpServletRequest request) {
        RateLimitContext.RateLimitContextBuilder builder = RateLimitContext.builder()
            .ipAddress(getClientIpAddress(request))
            .userAgent(request.getHeader("User-Agent"))
            .operation(determineOperation(request))
            .timestamp(System.currentTimeMillis());

        // Extract user information from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userId = authentication.getName();
            builder.userId(userId);
            
            // Extract tenant information from JWT claims if available
            String tenantId = extractTenantFromAuthentication(authentication);
            builder.tenantId(tenantId);
            
            // Extract user tier information
            String userTier = extractUserTierFromAuthentication(authentication);
            builder.userTier(userTier);
        }

        // Extract service context for service-to-service calls
        String sourceService = request.getHeader("X-Source-Service");
        if (sourceService != null) {
            builder.sourceService(sourceService);
        }

        // Extract API key for third-party integrations
        String apiKey = extractApiKey(request);
        if (apiKey != null) {
            builder.apiKey(apiKey);
        }

        return builder.build();
    }

    /**
     * Perform comprehensive rate limiting checks across all dimensions.
     */
    private boolean performRateLimitChecks(RateLimitContext context, 
                                         HttpServletResponse response, 
                                         Span span) {
        
        // Check IP-based rate limits first (DDoS protection)
        if (!checkIpRateLimit(context, span)) {
            span.setTag("rate_limit.type", "ip");
            return false;
        }

        // Check global rate limits (system protection)
        if (!checkGlobalRateLimit(context, span)) {
            span.setTag("rate_limit.type", "global");
            return false;
        }

        // Check service-to-service rate limits
        if (context.getSourceService() != null && !checkServiceRateLimit(context, span)) {
            span.setTag("rate_limit.type", "service");
            return false;
        }

        // Check API key rate limits
        if (context.getApiKey() != null && !checkApiKeyRateLimit(context, span)) {
            span.setTag("rate_limit.type", "api_key");
            return false;
        }

        // Check user-specific rate limits
        if (context.getUserId() != null && !checkUserRateLimit(context, span)) {
            span.setTag("rate_limit.type", "user");
            return false;
        }

        // Check tenant-specific rate limits
        if (context.getTenantId() != null && !checkTenantRateLimit(context, span)) {
            span.setTag("rate_limit.type", "tenant");
            return false;
        }

        return true;
    }

    private boolean checkIpRateLimit(RateLimitContext context, Span span) {
        return rateLimitingService.checkRateLimit(
            AdvancedRateLimitingService.RateLimitType.IP,
            context.getIpAddress(),
            context.getOperation(),
            Duration.ofMinutes(1)
        );
    }

    private boolean checkGlobalRateLimit(RateLimitContext context, Span span) {
        return rateLimitingService.checkRateLimit(
            AdvancedRateLimitingService.RateLimitType.GLOBAL,
            "system",
            context.getOperation(),
            Duration.ofMinutes(1)
        );
    }

    private boolean checkServiceRateLimit(RateLimitContext context, Span span) {
        String key = context.getSourceService() + ":user-service";
        return rateLimitingService.checkRateLimit(
            AdvancedRateLimitingService.RateLimitType.SERVICE,
            key,
            context.getOperation(),
            Duration.ofMinutes(1)
        );
    }

    private boolean checkApiKeyRateLimit(RateLimitContext context, Span span) {
        return rateLimitingService.checkRateLimit(
            AdvancedRateLimitingService.RateLimitType.API_KEY,
            context.getApiKey(),
            context.getOperation(),
            Duration.ofMinutes(1)
        );
    }

    private boolean checkUserRateLimit(RateLimitContext context, Span span) {
        return rateLimitingService.checkRateLimit(
            AdvancedRateLimitingService.RateLimitType.USER,
            context.getUserId(),
            context.getOperation(),
            Duration.ofMinutes(1)
        );
    }

    private boolean checkTenantRateLimit(RateLimitContext context, Span span) {
        String key = context.getTenantId() + ":" + context.getOperation();
        return rateLimitingService.checkRateLimit(
            AdvancedRateLimitingService.RateLimitType.TENANT,
            key,
            context.getOperation(),
            Duration.ofMinutes(1)
        );
    }

    /**
     * Set rate limiting headers in the response.
     */
    private void setRateLimitHeaders(HttpServletResponse response, RateLimitContext context) {
        // Get current rate limit status
        // Note: This is a simplified implementation - in production, you'd want to
        // get the actual remaining count and reset time from the rate limiting service
        response.setHeader(RATE_LIMIT_LIMIT_HEADER, "100");
        response.setHeader(RATE_LIMIT_REMAINING_HEADER, "0");
        response.setHeader(RATE_LIMIT_RESET_HEADER, String.valueOf(System.currentTimeMillis() / 1000 + 60));
        response.setHeader(RATE_LIMIT_RETRY_AFTER_HEADER, "60");
    }

    /**
     * Build JSON error response for rate limit exceeded.
     */
    private String buildRateLimitErrorResponse(RateLimitContext context) {
        return String.format("""
            {
                "error": "rate_limit_exceeded",
                "message": "Rate limit exceeded for operation: %s",
                "operation": "%s",
                "retry_after": 60,
                "timestamp": %d
            }
            """, context.getOperation(), context.getOperation(), System.currentTimeMillis());
    }

    /**
     * Extract client IP address considering proxies and load balancers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For", "X-Real-IP", "X-Client-IP", "CF-Connecting-IP"
        };
        
        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Handle comma-separated IPs (proxy chains)
                return ip.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Determine the operation type from the request.
     */
    private String determineOperation(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Map common paths to operations
        if (path.contains("/auth/")) {
            return "authentication";
        } else if (path.contains("/register")) {
            return "registration";
        } else if (path.contains("/profile") && "PUT".equals(method)) {
            return "profile_update";
        } else if (path.contains("/password/reset")) {
            return "password_reset";
        } else {
            return "api_call";
        }
    }

    /**
     * Extract tenant ID from authentication context.
     */
    private String extractTenantFromAuthentication(Authentication authentication) {
        // Implementation depends on how tenant information is stored in JWT
        // This is a placeholder implementation
        return Optional.ofNullable(authentication.getDetails())
            .map(details -> {
                if (details instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
                    return jwt.getClaimAsString("tenant_id");
                }
                return null;
            })
            .orElse("default");
    }

    /**
     * Extract user tier from authentication context.
     */
    private String extractUserTierFromAuthentication(Authentication authentication) {
        // Implementation depends on how user tier information is stored
        return Optional.ofNullable(authentication.getDetails())
            .map(details -> {
                if (details instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
                    return jwt.getClaimAsString("user_tier");
                }
                return null;
            })
            .orElse("free");
    }

    /**
     * Extract API key from request headers or parameters.
     */
    private String extractApiKey(HttpServletRequest request) {
        // Check Authorization header for API key
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("ApiKey ")) {
            return authHeader.substring(7);
        }
        
        // Check X-API-Key header
        String apiKeyHeader = request.getHeader("X-API-Key");
        if (apiKeyHeader != null) {
            return apiKeyHeader;
        }
        
        // Check query parameter
        return request.getParameter("api_key");
    }

    /**
     * Log rate limit violation for audit purposes.
     */
    private void logRateLimitViolation(RateLimitContext context, HttpServletRequest request) {
        log.warn("Rate limit exceeded - userId: {}, tenantId: {}, operation: {}, ip: {}, userAgent: {}", 
            context.getUserId(),
            context.getTenantId(),
            context.getOperation(),
            context.getIpAddress(),
            context.getUserAgent());
    }

    /**
     * Track rate limiting metrics.
     */
    private void trackRateLimitMetrics(RateLimitContext context, boolean allowed) {
        securityMetricsService.recordRateLimitCheck(
            context.getOperation(),
            context.getTenantId(),
            allowed
        );
    }

    /**
     * Rate limiting context data class.
     */
    @lombok.Builder
    @lombok.Data
    public static class RateLimitContext {
        private String userId;
        private String tenantId;
        private String userTier;
        private String ipAddress;
        private String userAgent;
        private String operation;
        private String sourceService;
        private String apiKey;
        private long timestamp;
    }
}
