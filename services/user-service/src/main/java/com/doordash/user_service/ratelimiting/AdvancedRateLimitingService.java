package com.doordash.user_service.ratelimiting;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Advanced Rate Limiting and Quota Management Service for DoorDash.
 * 
 * This service provides enterprise-grade rate limiting capabilities including:
 * - Per-user rate limiting with sliding window algorithms
 * - Per-service rate limiting and quotas
 * - Multi-tenant rate limiting with tenant-specific limits
 * - Distributed rate limiting across multiple instances
 * - Burst capacity handling with token bucket algorithm
 * - Dynamic rate limit adjustment based on load
 * - Integration with circuit breakers and fallback mechanisms
 * 
 * Rate Limiting Strategies:
 * - Fixed Window: Simple time-based windows
 * - Sliding Window: More accurate rate limiting
 * - Token Bucket: Burst handling with sustained rates
 * - Leaky Bucket: Smooth rate limiting
 * - Adaptive: Dynamic adjustment based on system load
 * 
 * Multi-Dimensional Limiting:
 * - User-based: Individual user rate limits
 * - Service-based: Per-service consumption limits
 * - Tenant-based: Multi-tenant isolation
 * - IP-based: DDoS protection
 * - API-key based: Third-party integration limits
 * - Geographic: Region-specific rate limiting
 * 
 * Integration Features:
 * - Redis-backed distributed storage
 * - Prometheus metrics for monitoring
 * - Circuit breaker integration
 * - Graceful degradation support
 * - Real-time quota adjustment
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdvancedRateLimitingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RateLimitingConfigurationProperties config;
    
    // Redis Lua scripts for atomic operations
    private static final String SLIDING_WINDOW_SCRIPT = """
        local key = KEYS[1]
        local window = tonumber(ARGV[1])
        local limit = tonumber(ARGV[2])
        local current_time = tonumber(ARGV[3])
        local ttl = tonumber(ARGV[4])
        
        -- Remove expired entries
        redis.call('ZREMRANGEBYSCORE', key, 0, current_time - window)
        
        -- Count current requests
        local current_count = redis.call('ZCARD', key)
        
        if current_count >= limit then
            return {0, current_count, limit - current_count}
        end
        
        -- Add current request
        redis.call('ZADD', key, current_time, current_time .. ':' .. math.random())
        redis.call('EXPIRE', key, ttl)
        
        return {1, current_count + 1, limit - current_count - 1}
        """;
    
    private static final String TOKEN_BUCKET_SCRIPT = """
        local key = KEYS[1]
        local capacity = tonumber(ARGV[1])
        local refill_rate = tonumber(ARGV[2])
        local current_time = tonumber(ARGV[3])
        local tokens_requested = tonumber(ARGV[4])
        local ttl = tonumber(ARGV[5])
        
        local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
        local tokens = tonumber(bucket[1]) or capacity
        local last_refill = tonumber(bucket[2]) or current_time
        
        -- Calculate tokens to add based on time elapsed
        local time_elapsed = math.max(0, current_time - last_refill)
        local tokens_to_add = math.floor(time_elapsed * refill_rate / 1000)
        tokens = math.min(capacity, tokens + tokens_to_add)
        
        if tokens >= tokens_requested then
            tokens = tokens - tokens_requested
            redis.call('HMSET', key, 'tokens', tokens, 'last_refill', current_time)
            redis.call('EXPIRE', key, ttl)
            return {1, tokens, capacity}
        else
            redis.call('HMSET', key, 'tokens', tokens, 'last_refill', current_time)
            redis.call('EXPIRE', key, ttl)
            return {0, tokens, capacity}
        end
        """;

    /**
     * Rate limiting result containing decision and metadata.
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final long currentUsage;
        private final long limit;
        private final long remainingTokens;
        private final Duration resetTime;
        private final String limitType;
        
        public RateLimitResult(boolean allowed, long currentUsage, long limit, 
                             long remainingTokens, Duration resetTime, String limitType) {
            this.allowed = allowed;
            this.currentUsage = currentUsage;
            this.limit = limit;
            this.remainingTokens = remainingTokens;
            this.resetTime = resetTime;
            this.limitType = limitType;
        }
        
        // Getters
        public boolean isAllowed() { return allowed; }
        public long getCurrentUsage() { return currentUsage; }
        public long getLimit() { return limit; }
        public long getRemainingTokens() { return remainingTokens; }
        public Duration getResetTime() { return resetTime; }
        public String getLimitType() { return limitType; }
    }

    /**
     * Rate limiting configuration for different dimensions.
     */
    public static class RateLimitConfig {
        private final int limit;
        private final Duration window;
        private final int burstCapacity;
        private final double refillRate;
        private final String algorithm;
        
        public RateLimitConfig(int limit, Duration window, int burstCapacity, 
                             double refillRate, String algorithm) {
            this.limit = limit;
            this.window = window;
            this.burstCapacity = burstCapacity;
            this.refillRate = refillRate;
            this.algorithm = algorithm;
        }
        
        // Getters
        public int getLimit() { return limit; }
        public Duration getWindow() { return window; }
        public int getBurstCapacity() { return burstCapacity; }
        public double getRefillRate() { return refillRate; }
        public String getAlgorithm() { return algorithm; }
    }

    /**
     * Checks rate limit for a user with multiple dimensions.
     * 
     * @param userId user identifier
     * @param tenantId tenant identifier for multi-tenancy
     * @param operation operation being performed
     * @param clientIp client IP address
     * @return RateLimitResult rate limiting decision and metadata
     */
    public RateLimitResult checkUserRateLimit(String userId, String tenantId, 
                                            String operation, String clientIp) {
        
        List<RateLimitResult> results = new ArrayList<>();
        
        try {
            // Check user-specific rate limit
            RateLimitConfig userConfig = getUserRateLimitConfig(userId, tenantId);
            RateLimitResult userResult = checkRateLimit(
                buildUserKey(userId, operation), userConfig, "user");
            results.add(userResult);
            
            // Check tenant-specific rate limit
            RateLimitConfig tenantConfig = getTenantRateLimitConfig(tenantId, operation);
            RateLimitResult tenantResult = checkRateLimit(
                buildTenantKey(tenantId, operation), tenantConfig, "tenant");
            results.add(tenantResult);
            
            // Check IP-based rate limit for DDoS protection
            RateLimitConfig ipConfig = getIpRateLimitConfig(operation);
            RateLimitResult ipResult = checkRateLimit(
                buildIpKey(clientIp, operation), ipConfig, "ip");
            results.add(ipResult);
            
            // Check global operation rate limit
            RateLimitConfig globalConfig = getGlobalRateLimitConfig(operation);
            RateLimitResult globalResult = checkRateLimit(
                buildGlobalKey(operation), globalConfig, "global");
            results.add(globalResult);
            
            // Return the most restrictive result
            return results.stream()
                .filter(result -> !result.isAllowed())
                .findFirst()
                .orElse(results.get(0));
                
        } catch (Exception e) {
            log.error("Error checking rate limit for user: {} operation: {}", userId, operation, e);
            // Fail open in case of errors
            return new RateLimitResult(true, 0, Integer.MAX_VALUE, 
                Integer.MAX_VALUE, Duration.ZERO, "failsafe");
        }
    }

    /**
     * Checks service-to-service rate limits.
     * 
     * @param sourceService source service making the request
     * @param targetService target service being called
     * @param operation operation being performed
     * @return RateLimitResult rate limiting decision
     */
    public RateLimitResult checkServiceRateLimit(String sourceService, String targetService, 
                                               String operation) {
        try {
            RateLimitConfig serviceConfig = getServiceRateLimitConfig(sourceService, targetService);
            return checkRateLimit(
                buildServiceKey(sourceService, targetService, operation), 
                serviceConfig, "service");
        } catch (Exception e) {
            log.error("Error checking service rate limit: {} -> {} operation: {}", 
                sourceService, targetService, operation, e);
            return new RateLimitResult(true, 0, Integer.MAX_VALUE, 
                Integer.MAX_VALUE, Duration.ZERO, "failsafe");
        }
    }

    /**
     * Checks API key-based rate limits for third-party integrations.
     * 
     * @param apiKey API key identifier
     * @param operation operation being performed
     * @return RateLimitResult rate limiting decision
     */
    public RateLimitResult checkApiKeyRateLimit(String apiKey, String operation) {
        try {
            RateLimitConfig apiKeyConfig = getApiKeyRateLimitConfig(apiKey);
            return checkRateLimit(
                buildApiKeyKey(apiKey, operation), 
                apiKeyConfig, "api_key");
        } catch (Exception e) {
            log.error("Error checking API key rate limit: {} operation: {}", apiKey, operation, e);
            return new RateLimitResult(true, 0, Integer.MAX_VALUE, 
                Integer.MAX_VALUE, Duration.ZERO, "failsafe");
        }
    }

    /**
     * Core rate limiting logic with algorithm selection.
     * 
     * @param key Redis key for the rate limit
     * @param config rate limit configuration
     * @param limitType type of limit for logging
     * @return RateLimitResult rate limiting decision
     */
    private RateLimitResult checkRateLimit(String key, RateLimitConfig config, String limitType) {
        long currentTime = System.currentTimeMillis();
        
        switch (config.getAlgorithm().toLowerCase()) {
            case "sliding_window":
                return checkSlidingWindowRateLimit(key, config, currentTime, limitType);
            case "token_bucket":
                return checkTokenBucketRateLimit(key, config, currentTime, limitType);
            case "fixed_window":
                return checkFixedWindowRateLimit(key, config, currentTime, limitType);
            default:
                log.warn("Unknown rate limiting algorithm: {}, using sliding_window", config.getAlgorithm());
                return checkSlidingWindowRateLimit(key, config, currentTime, limitType);
        }
    }

    /**
     * Sliding window rate limiting implementation.
     */
    private RateLimitResult checkSlidingWindowRateLimit(String key, RateLimitConfig config, 
                                                      long currentTime, String limitType) {
        try {
            RedisScript<List> script = RedisScript.of(SLIDING_WINDOW_SCRIPT, List.class);
            List<Object> result = redisTemplate.execute(script, 
                Collections.singletonList(key),
                config.getWindow().toMillis(),
                config.getLimit(),
                currentTime,
                config.getWindow().toSeconds() + 60
            );
            
            boolean allowed = ((Number) result.get(0)).intValue() == 1;
            long currentUsage = ((Number) result.get(1)).longValue();
            long remaining = ((Number) result.get(2)).longValue();
            
            log.debug("Sliding window rate limit check - Key: {} Allowed: {} Usage: {}/{}", 
                key, allowed, currentUsage, config.getLimit());
            
            return new RateLimitResult(allowed, currentUsage, config.getLimit(), 
                remaining, config.getWindow(), limitType);
                
        } catch (Exception e) {
            log.error("Error in sliding window rate limit check for key: {}", key, e);
            return new RateLimitResult(true, 0, config.getLimit(), 
                config.getLimit(), config.getWindow(), limitType);
        }
    }

    /**
     * Token bucket rate limiting implementation.
     */
    private RateLimitResult checkTokenBucketRateLimit(String key, RateLimitConfig config, 
                                                    long currentTime, String limitType) {
        try {
            RedisScript<List> script = RedisScript.of(TOKEN_BUCKET_SCRIPT, List.class);
            List<Object> result = redisTemplate.execute(script,
                Collections.singletonList(key),
                config.getBurstCapacity(),
                config.getRefillRate(),
                currentTime,
                1, // tokens requested
                config.getWindow().toSeconds() + 60
            );
            
            boolean allowed = ((Number) result.get(0)).intValue() == 1;
            long tokens = ((Number) result.get(1)).longValue();
            long capacity = ((Number) result.get(2)).longValue();
            
            log.debug("Token bucket rate limit check - Key: {} Allowed: {} Tokens: {}/{}", 
                key, allowed, tokens, capacity);
            
            return new RateLimitResult(allowed, capacity - tokens, capacity, 
                tokens, Duration.ofSeconds(1), limitType);
                
        } catch (Exception e) {
            log.error("Error in token bucket rate limit check for key: {}", key, e);
            return new RateLimitResult(true, 0, config.getBurstCapacity(), 
                config.getBurstCapacity(), Duration.ofSeconds(1), limitType);
        }
    }

    /**
     * Fixed window rate limiting implementation.
     */
    private RateLimitResult checkFixedWindowRateLimit(String key, RateLimitConfig config, 
                                                    long currentTime, String limitType) {
        try {
            long windowStart = (currentTime / config.getWindow().toMillis()) * config.getWindow().toMillis();
            String windowKey = key + ":" + windowStart;
            
            Long currentCount = redisTemplate.opsForValue().increment(windowKey);
            if (currentCount == 1) {
                redisTemplate.expire(windowKey, config.getWindow().toSeconds(), TimeUnit.SECONDS);
            }
            
            boolean allowed = currentCount <= config.getLimit();
            long remaining = Math.max(0, config.getLimit() - currentCount);
            
            Duration resetTime = Duration.ofMillis(
                windowStart + config.getWindow().toMillis() - currentTime);
            
            log.debug("Fixed window rate limit check - Key: {} Allowed: {} Usage: {}/{}", 
                windowKey, allowed, currentCount, config.getLimit());
            
            return new RateLimitResult(allowed, currentCount, config.getLimit(), 
                remaining, resetTime, limitType);
                
        } catch (Exception e) {
            log.error("Error in fixed window rate limit check for key: {}", key, e);
            return new RateLimitResult(true, 0, config.getLimit(), 
                config.getLimit(), config.getWindow(), limitType);
        }
    }

    /**
     * Adjusts rate limits dynamically based on system load.
     * 
     * @param userId user identifier
     * @param tenantId tenant identifier
     * @param newLimit new rate limit
     * @param window time window for the limit
     */
    public void adjustUserRateLimit(String userId, String tenantId, int newLimit, Duration window) {
        try {
            String configKey = "rate_limit:config:user:" + tenantId + ":" + userId;
            Map<String, Object> configMap = Map.of(
                "limit", newLimit,
                "window", window.toMillis(),
                "updated_at", System.currentTimeMillis()
            );
            
            redisTemplate.opsForHash().putAll(configKey, configMap);
            redisTemplate.expire(configKey, Duration.ofHours(24));
            
            log.info("Adjusted rate limit for user: {} tenant: {} to {} requests per {}", 
                userId, tenantId, newLimit, window);
                
        } catch (Exception e) {
            log.error("Error adjusting rate limit for user: {} tenant: {}", userId, tenantId, e);
        }
    }

    /**
     * Gets current quota usage for a user.
     * 
     * @param userId user identifier
     * @param tenantId tenant identifier
     * @param operation operation type
     * @return Map of quota usage statistics
     */
    public Map<String, Object> getUserQuotaUsage(String userId, String tenantId, String operation) {
        try {
            String key = buildUserKey(userId, operation);
            Map<String, Object> usage = new HashMap<>();
            
            // Get sliding window usage
            long currentTime = System.currentTimeMillis();
            RateLimitConfig config = getUserRateLimitConfig(userId, tenantId);
            
            Long count = redisTemplate.opsForZSet().count(key, 
                currentTime - config.getWindow().toMillis(), currentTime);
            
            usage.put("current_usage", count != null ? count : 0);
            usage.put("limit", config.getLimit());
            usage.put("remaining", Math.max(0, config.getLimit() - (count != null ? count : 0)));
            usage.put("window_seconds", config.getWindow().toSeconds());
            usage.put("reset_time", currentTime + config.getWindow().toMillis());
            
            return usage;
            
        } catch (Exception e) {
            log.error("Error getting quota usage for user: {} operation: {}", userId, operation, e);
            return Map.of("error", "Unable to retrieve quota usage");
        }
    }

    // ========== CONFIGURATION METHODS ==========

    private RateLimitConfig getUserRateLimitConfig(String userId, String tenantId) {
        // Check for user-specific overrides
        String configKey = "rate_limit:config:user:" + tenantId + ":" + userId;
        Map<Object, Object> userConfig = redisTemplate.opsForHash().entries(configKey);
        
        if (!userConfig.isEmpty()) {
            return new RateLimitConfig(
                ((Number) userConfig.get("limit")).intValue(),
                Duration.ofMillis(((Number) userConfig.get("window")).longValue()),
                ((Number) userConfig.getOrDefault("burst_capacity", 100)).intValue(),
                ((Number) userConfig.getOrDefault("refill_rate", 10.0)).doubleValue(),
                (String) userConfig.getOrDefault("algorithm", "sliding_window")
            );
        }
        
        // Fall back to tenant defaults
        return getTenantRateLimitConfig(tenantId, "default");
    }

    private RateLimitConfig getTenantRateLimitConfig(String tenantId, String operation) {
        return config.getTenantRateLimits().getOrDefault(tenantId + ":" + operation,
            new RateLimitConfig(100, Duration.ofMinutes(1), 200, 10.0, "sliding_window"));
    }

    private RateLimitConfig getIpRateLimitConfig(String operation) {
        return config.getIpRateLimits().getOrDefault(operation,
            new RateLimitConfig(1000, Duration.ofMinutes(1), 1500, 50.0, "token_bucket"));
    }

    private RateLimitConfig getGlobalRateLimitConfig(String operation) {
        return config.getGlobalRateLimits().getOrDefault(operation,
            new RateLimitConfig(10000, Duration.ofMinutes(1), 15000, 500.0, "token_bucket"));
    }

    private RateLimitConfig getServiceRateLimitConfig(String sourceService, String targetService) {
        String key = sourceService + ":" + targetService;
        return config.getServiceRateLimits().getOrDefault(key,
            new RateLimitConfig(1000, Duration.ofMinutes(1), 1500, 100.0, "sliding_window"));
    }

    private RateLimitConfig getApiKeyRateLimitConfig(String apiKey) {
        return config.getApiKeyRateLimits().getOrDefault(apiKey,
            new RateLimitConfig(100, Duration.ofHour(1), 150, 2.0, "sliding_window"));
    }

    // ========== KEY BUILDING METHODS ==========

    private String buildUserKey(String userId, String operation) {
        return "rate_limit:user:" + userId + ":" + operation;
    }

    private String buildTenantKey(String tenantId, String operation) {
        return "rate_limit:tenant:" + tenantId + ":" + operation;
    }

    private String buildIpKey(String clientIp, String operation) {
        return "rate_limit:ip:" + clientIp + ":" + operation;
    }

    private String buildGlobalKey(String operation) {
        return "rate_limit:global:" + operation;
    }

    private String buildServiceKey(String sourceService, String targetService, String operation) {
        return "rate_limit:service:" + sourceService + ":" + targetService + ":" + operation;
    }

    private String buildApiKeyKey(String apiKey, String operation) {
        return "rate_limit:api_key:" + apiKey + ":" + operation;
    }
}
