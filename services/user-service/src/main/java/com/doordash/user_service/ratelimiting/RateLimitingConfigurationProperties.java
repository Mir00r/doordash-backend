package com.doordash.user_service.ratelimiting;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Rate Limiting Configuration Properties for DoorDash Services.
 * 
 * Provides comprehensive configuration for multi-dimensional rate limiting including:
 * - User-specific rate limits with tenant isolation
 * - Service-to-service rate limiting quotas
 * - API key-based rate limiting for third-party integrations
 * - IP-based rate limiting for DDoS protection
 * - Global rate limiting for system protection
 * - Dynamic configuration updates
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.rate-limiting")
public class RateLimitingConfigurationProperties {

    /**
     * Enable/disable rate limiting globally.
     */
    private boolean enabled = true;

    /**
     * Default rate limiting algorithm (sliding_window, token_bucket, fixed_window).
     */
    private String defaultAlgorithm = "sliding_window";

    /**
     * Redis key prefix for rate limiting data.
     */
    private String redisKeyPrefix = "doordash:rate_limit";

    /**
     * Default TTL for rate limiting data in Redis.
     */
    private Duration defaultTtl = Duration.ofHours(1);

    /**
     * Tenant-specific rate limits (tenantId:operation -> RateLimitConfig).
     */
    private Map<String, AdvancedRateLimitingService.RateLimitConfig> tenantRateLimits = new HashMap<>();

    /**
     * Service-to-service rate limits (sourceService:targetService -> RateLimitConfig).
     */
    private Map<String, AdvancedRateLimitingService.RateLimitConfig> serviceRateLimits = new HashMap<>();

    /**
     * API key-based rate limits (apiKey -> RateLimitConfig).
     */
    private Map<String, AdvancedRateLimitingService.RateLimitConfig> apiKeyRateLimits = new HashMap<>();

    /**
     * IP-based rate limits (operation -> RateLimitConfig).
     */
    private Map<String, AdvancedRateLimitingService.RateLimitConfig> ipRateLimits = new HashMap<>();

    /**
     * Global rate limits (operation -> RateLimitConfig).
     */
    private Map<String, AdvancedRateLimitingService.RateLimitConfig> globalRateLimits = new HashMap<>();

    /**
     * Rate limiting configurations for different user tiers.
     */
    private Map<String, TierConfig> userTiers = new HashMap<>();

    /**
     * Circuit breaker configuration for rate limiting.
     */
    private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();

    /**
     * Metrics and monitoring configuration.
     */
    private MetricsConfig metrics = new MetricsConfig();

    /**
     * Configuration for different user tiers (free, premium, enterprise).
     */
    @Data
    public static class TierConfig {
        private int baseLimit = 100;
        private Duration window = Duration.ofMinutes(1);
        private int burstCapacity = 150;
        private double refillRate = 10.0;
        private String algorithm = "sliding_window";
        private Map<String, OperationConfig> operations = new HashMap<>();
    }

    /**
     * Operation-specific configuration within a tier.
     */
    @Data
    public static class OperationConfig {
        private int limit;
        private Duration window;
        private int burstCapacity;
        private double refillRate;
        private String algorithm;
    }

    /**
     * Circuit breaker configuration for rate limiting service.
     */
    @Data
    public static class CircuitBreakerConfig {
        private boolean enabled = true;
        private int failureThreshold = 5;
        private Duration timeout = Duration.ofSeconds(30);
        private int successThreshold = 3;
    }

    /**
     * Metrics and monitoring configuration.
     */
    @Data
    public static class MetricsConfig {
        private boolean enabled = true;
        private Duration aggregationWindow = Duration.ofMinutes(1);
        private boolean includeUserMetrics = true;
        private boolean includeTenantMetrics = true;
        private boolean includeServiceMetrics = true;
    }

    /**
     * Initialize default configurations if not provided.
     */
    public void initializeDefaults() {
        initializeDefaultTenantLimits();
        initializeDefaultServiceLimits();
        initializeDefaultIpLimits();
        initializeDefaultGlobalLimits();
        initializeDefaultUserTiers();
    }

    private void initializeDefaultTenantLimits() {
        if (tenantRateLimits.isEmpty()) {
            // Default tenant limits for different operations
            tenantRateLimits.put("default:authentication", 
                new AdvancedRateLimitingService.RateLimitConfig(100, Duration.ofMinutes(1), 150, 10.0, "sliding_window"));
            tenantRateLimits.put("default:registration", 
                new AdvancedRateLimitingService.RateLimitConfig(10, Duration.ofMinutes(1), 15, 1.0, "sliding_window"));
            tenantRateLimits.put("default:profile_update", 
                new AdvancedRateLimitingService.RateLimitConfig(50, Duration.ofMinutes(1), 75, 5.0, "sliding_window"));
            tenantRateLimits.put("default:password_reset", 
                new AdvancedRateLimitingService.RateLimitConfig(5, Duration.ofMinutes(15), 10, 0.5, "sliding_window"));
        }
    }

    private void initializeDefaultServiceLimits() {
        if (serviceRateLimits.isEmpty()) {
            // Service-to-service rate limits
            serviceRateLimits.put("api-gateway:user-service", 
                new AdvancedRateLimitingService.RateLimitConfig(1000, Duration.ofMinutes(1), 1500, 100.0, "token_bucket"));
            serviceRateLimits.put("auth-service:user-service", 
                new AdvancedRateLimitingService.RateLimitConfig(500, Duration.ofMinutes(1), 750, 50.0, "sliding_window"));
            serviceRateLimits.put("order-service:user-service", 
                new AdvancedRateLimitingService.RateLimitConfig(200, Duration.ofMinutes(1), 300, 20.0, "sliding_window"));
            serviceRateLimits.put("notification-service:user-service", 
                new AdvancedRateLimitingService.RateLimitConfig(100, Duration.ofMinutes(1), 150, 10.0, "sliding_window"));
        }
    }

    private void initializeDefaultIpLimits() {
        if (ipRateLimits.isEmpty()) {
            // IP-based rate limits for DDoS protection
            ipRateLimits.put("authentication", 
                new AdvancedRateLimitingService.RateLimitConfig(50, Duration.ofMinutes(1), 75, 5.0, "token_bucket"));
            ipRateLimits.put("registration", 
                new AdvancedRateLimitingService.RateLimitConfig(5, Duration.ofMinutes(5), 10, 1.0, "sliding_window"));
            ipRateLimits.put("api_call", 
                new AdvancedRateLimitingService.RateLimitConfig(1000, Duration.ofMinutes(1), 1500, 100.0, "token_bucket"));
        }
    }

    private void initializeDefaultGlobalLimits() {
        if (globalRateLimits.isEmpty()) {
            // Global system protection limits
            globalRateLimits.put("authentication", 
                new AdvancedRateLimitingService.RateLimitConfig(10000, Duration.ofMinutes(1), 15000, 1000.0, "token_bucket"));
            globalRateLimits.put("registration", 
                new AdvancedRateLimitingService.RateLimitConfig(1000, Duration.ofMinutes(1), 1500, 100.0, "sliding_window"));
            globalRateLimits.put("profile_update", 
                new AdvancedRateLimitingService.RateLimitConfig(5000, Duration.ofMinutes(1), 7500, 500.0, "token_bucket"));
        }
    }

    private void initializeDefaultUserTiers() {
        if (userTiers.isEmpty()) {
            // Free tier
            TierConfig freeTier = new TierConfig();
            freeTier.setBaseLimit(100);
            freeTier.setWindow(Duration.ofMinutes(1));
            freeTier.setBurstCapacity(150);
            freeTier.setRefillRate(10.0);
            freeTier.setAlgorithm("sliding_window");
            userTiers.put("free", freeTier);

            // Premium tier
            TierConfig premiumTier = new TierConfig();
            premiumTier.setBaseLimit(500);
            premiumTier.setWindow(Duration.ofMinutes(1));
            premiumTier.setBurstCapacity(750);
            premiumTier.setRefillRate(50.0);
            premiumTier.setAlgorithm("token_bucket");
            userTiers.put("premium", premiumTier);

            // Enterprise tier
            TierConfig enterpriseTier = new TierConfig();
            enterpriseTier.setBaseLimit(2000);
            enterpriseTier.setWindow(Duration.ofMinutes(1));
            enterpriseTier.setBurstCapacity(3000);
            enterpriseTier.setRefillRate(200.0);
            enterpriseTier.setAlgorithm("token_bucket");
            userTiers.put("enterprise", enterpriseTier);
        }
    }
}
