package com.doordash.user_service.observability.metrics;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Comprehensive Metrics Service for DoorDash User Service.
 * 
 * This service provides enterprise-grade metrics collection and monitoring including:
 * - Business metrics for user operations and workflows
 * - Security metrics for authentication and authorization events
 * - Performance metrics for latency and throughput monitoring
 * - System metrics for resource utilization tracking
 * - Custom metrics for domain-specific monitoring
 * - Integration with Prometheus and Grafana dashboards
 * 
 * Metric Categories:
 * - Counter metrics for event counting (requests, errors, successes)
 * - Timer metrics for latency and duration tracking
 * - Gauge metrics for current state monitoring (active sessions, queue sizes)
 * - Distribution summaries for value distribution analysis
 * - Custom meters for complex metric scenarios
 * 
 * Business Intelligence:
 * - User registration and authentication rates
 * - Profile update frequencies and patterns
 * - Security event monitoring and alerting
 * - Performance SLA tracking and reporting
 * - Resource utilization and capacity planning
 * 
 * Integration Features:
 * - Prometheus exposition format for scraping
 * - Grafana dashboard compatibility
 * - Alert manager integration for threshold-based alerting
 * - Custom tags for filtering and aggregation
 * - Time-series data retention and rollup policies
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;
    
    // Cache for frequently used metrics to improve performance
    private final Map<String, Counter> counterCache = new ConcurrentHashMap<>();
    private final Map<String, Timer> timerCache = new ConcurrentHashMap<>();
    private final Map<String, Gauge> gaugeCache = new ConcurrentHashMap<>();
    private final Map<String, DistributionSummary> summaryCache = new ConcurrentHashMap<>();

    // Common metric prefixes for organization
    private static final String USER_METRIC_PREFIX = "doordash.user";
    private static final String SECURITY_METRIC_PREFIX = "doordash.security";
    private static final String PERFORMANCE_METRIC_PREFIX = "doordash.performance";
    private static final String BUSINESS_METRIC_PREFIX = "doordash.business";
    private static final String SYSTEM_METRIC_PREFIX = "doordash.system";

    /**
     * Records a counter metric with optional tags.
     * 
     * @param metricName name of the metric
     * @param tags optional tags for metric filtering
     */
    public void incrementCounter(String metricName, String... tags) {
        String cacheKey = buildCacheKey(metricName, tags);
        Counter counter = counterCache.computeIfAbsent(cacheKey, k -> 
            Counter.builder(metricName)
                .tags(tags)
                .description("Counter metric for " + metricName)
                .register(meterRegistry)
        );
        counter.increment();
        log.debug("Incremented counter: {} with tags: {}", metricName, Arrays.toString(tags));
    }

    /**
     * Records a counter metric with specific increment value.
     * 
     * @param metricName name of the metric
     * @param increment value to increment by
     * @param tags optional tags for metric filtering
     */
    public void incrementCounter(String metricName, double increment, String... tags) {
        String cacheKey = buildCacheKey(metricName, tags);
        Counter counter = counterCache.computeIfAbsent(cacheKey, k -> 
            Counter.builder(metricName)
                .tags(tags)
                .description("Counter metric for " + metricName)
                .register(meterRegistry)
        );
        counter.increment(increment);
        log.debug("Incremented counter: {} by {} with tags: {}", metricName, increment, Arrays.toString(tags));
    }

    /**
     * Records timing information for an operation.
     * 
     * @param metricName name of the timing metric
     * @param operation operation to time
     * @param tags optional tags for metric filtering
     * @param <T> return type of the operation
     * @return T result of the operation
     */
    public <T> T recordTimer(String metricName, Supplier<T> operation, String... tags) {
        String cacheKey = buildCacheKey(metricName, tags);
        Timer timer = timerCache.computeIfAbsent(cacheKey, k -> 
            Timer.builder(metricName)
                .tags(tags)
                .description("Timer metric for " + metricName)
                .register(meterRegistry)
        );
        
        return timer.recordCallable(() -> {
            log.debug("Starting timed operation: {} with tags: {}", metricName, Arrays.toString(tags));
            T result = operation.get();
            log.debug("Completed timed operation: {}", metricName);
            return result;
        });
    }

    /**
     * Records a specific duration for a metric.
     * 
     * @param metricName name of the timing metric
     * @param duration duration to record
     * @param unit time unit of the duration
     * @param tags optional tags for metric filtering
     */
    public void recordTimer(String metricName, long duration, TimeUnit unit, String... tags) {
        String cacheKey = buildCacheKey(metricName, tags);
        Timer timer = timerCache.computeIfAbsent(cacheKey, k -> 
            Timer.builder(metricName)
                .tags(tags)
                .description("Timer metric for " + metricName)
                .register(meterRegistry)
        );
        timer.record(duration, unit);
        log.debug("Recorded timer: {} duration: {} {} with tags: {}", metricName, duration, unit, Arrays.toString(tags));
    }

    /**
     * Records a gauge metric with a numeric value supplier.
     * 
     * @param metricName name of the gauge metric
     * @param valueSupplier supplier for the gauge value
     * @param tags optional tags for metric filtering
     */
    public void recordGauge(String metricName, Supplier<Number> valueSupplier, String... tags) {
        String cacheKey = buildCacheKey(metricName, tags);
        gaugeCache.computeIfAbsent(cacheKey, k -> 
            Gauge.builder(metricName)
                .tags(tags)
                .description("Gauge metric for " + metricName)
                .register(meterRegistry, valueSupplier, value -> value.get().doubleValue())
        );
        log.debug("Registered gauge: {} with tags: {}", metricName, Arrays.toString(tags));
    }

    /**
     * Records a distribution summary metric.
     * 
     * @param metricName name of the summary metric
     * @param value value to record
     * @param tags optional tags for metric filtering
     */
    public void recordSummary(String metricName, double value, String... tags) {
        String cacheKey = buildCacheKey(metricName, tags);
        DistributionSummary summary = summaryCache.computeIfAbsent(cacheKey, k -> 
            DistributionSummary.builder(metricName)
                .tags(tags)
                .description("Distribution summary for " + metricName)
                .register(meterRegistry)
        );
        summary.record(value);
        log.debug("Recorded summary: {} value: {} with tags: {}", metricName, value, Arrays.toString(tags));
    }

    // ========== USER SERVICE SPECIFIC METRICS ==========

    /**
     * Records user registration metrics.
     * 
     * @param successful whether registration was successful
     * @param registrationMethod method used for registration (email, social, etc.)
     */
    public void recordUserRegistration(boolean successful, String registrationMethod) {
        String status = successful ? "success" : "failure";
        incrementCounter(USER_METRIC_PREFIX + ".registration.total", 
            "status", status, "method", registrationMethod);
        
        if (successful) {
            log.info("User registration successful via method: {}", registrationMethod);
        } else {
            log.warn("User registration failed via method: {}", registrationMethod);
        }
    }

    /**
     * Records user authentication metrics.
     * 
     * @param successful whether authentication was successful
     * @param authMethod authentication method (password, oauth, etc.)
     * @param userId user identifier (for successful auth)
     */
    public void recordUserAuthentication(boolean successful, String authMethod, String userId) {
        String status = successful ? "success" : "failure";
        incrementCounter(USER_METRIC_PREFIX + ".authentication.total", 
            "status", status, "method", authMethod);
        
        if (successful && userId != null) {
            incrementCounter(USER_METRIC_PREFIX + ".authentication.unique_users", 
                "method", authMethod, "user_id", userId);
            log.info("User authentication successful for user: {} via method: {}", userId, authMethod);
        } else {
            log.warn("User authentication failed via method: {}", authMethod);
        }
    }

    /**
     * Records user profile operation metrics.
     * 
     * @param operation type of profile operation (view, update, delete)
     * @param successful whether operation was successful
     * @param userId user identifier
     */
    public void recordUserProfileOperation(String operation, boolean successful, String userId) {
        String status = successful ? "success" : "failure";
        incrementCounter(USER_METRIC_PREFIX + ".profile." + operation, 
            "status", status, "user_id", userId);
        
        log.debug("User profile {} operation {} for user: {}", operation, status, userId);
    }

    /**
     * Times user service operations for performance monitoring.
     * 
     * @param operation operation name
     * @param operationSupplier operation to execute and time
     * @param userId user identifier
     * @param <T> return type
     * @return T result of the operation
     */
    public <T> T timeUserOperation(String operation, Supplier<T> operationSupplier, String userId) {
        return recordTimer(USER_METRIC_PREFIX + ".operation.duration", 
            operationSupplier, "operation", operation, "user_id", userId);
    }

    // ========== SECURITY METRICS ==========

    /**
     * Records security event metrics for monitoring and alerting.
     * 
     * @param eventType type of security event (login_failure, rate_limit_exceeded, etc.)
     * @param severity severity level (low, medium, high, critical)
     * @param userId user identifier (if applicable)
     * @param sourceIp source IP address
     */
    public void recordSecurityEvent(String eventType, String severity, String userId, String sourceIp) {
        incrementCounter(SECURITY_METRIC_PREFIX + ".events.total", 
            "event_type", eventType, "severity", severity);
        
        if (userId != null) {
            incrementCounter(SECURITY_METRIC_PREFIX + ".events.by_user", 
                "event_type", eventType, "user_id", userId);
        }
        
        if (sourceIp != null) {
            incrementCounter(SECURITY_METRIC_PREFIX + ".events.by_ip", 
                "event_type", eventType, "source_ip", sourceIp);
        }
        
        log.warn("Security event recorded: {} severity: {} user: {} ip: {}", 
            eventType, severity, userId, sourceIp);
    }

    /**
     * Records JWT token metrics for monitoring token usage and validation.
     * 
     * @param operation token operation (validate, refresh, revoke)
     * @param successful whether operation was successful
     * @param tokenType type of token (access, refresh)
     */
    public void recordJwtTokenOperation(String operation, boolean successful, String tokenType) {
        String status = successful ? "success" : "failure";
        incrementCounter(SECURITY_METRIC_PREFIX + ".jwt." + operation, 
            "status", status, "token_type", tokenType);
        
        log.debug("JWT token {} operation {}: token_type={}", operation, status, tokenType);
    }

    /**
     * Records rate limiting metrics for monitoring and capacity planning.
     * 
     * @param limited whether request was rate limited
     * @param rateLimitType type of rate limit (ip, user, api_key)
     * @param identifier identifier that was rate limited
     */
    public void recordRateLimit(boolean limited, String rateLimitType, String identifier) {
        if (limited) {
            incrementCounter(SECURITY_METRIC_PREFIX + ".rate_limit.exceeded", 
                "type", rateLimitType, "identifier", identifier);
            log.warn("Rate limit exceeded: type={} identifier={}", rateLimitType, identifier);
        } else {
            incrementCounter(SECURITY_METRIC_PREFIX + ".rate_limit.allowed", 
                "type", rateLimitType);
        }
    }

    // ========== PERFORMANCE METRICS ==========

    /**
     * Records database operation performance metrics.
     * 
     * @param operation database operation type
     * @param operationSupplier operation to execute and time
     * @param tableName database table name
     * @param <T> return type
     * @return T result of the operation
     */
    public <T> T timeDatabaseOperation(String operation, Supplier<T> operationSupplier, String tableName) {
        return recordTimer(PERFORMANCE_METRIC_PREFIX + ".database.operation.duration", 
            operationSupplier, "operation", operation, "table", tableName);
    }

    /**
     * Records HTTP client operation performance metrics.
     * 
     * @param method HTTP method
     * @param operationSupplier operation to execute and time
     * @param service target service name
     * @param <T> return type
     * @return T result of the operation
     */
    public <T> T timeHttpClientOperation(String method, Supplier<T> operationSupplier, String service) {
        return recordTimer(PERFORMANCE_METRIC_PREFIX + ".http_client.request.duration", 
            operationSupplier, "method", method, "service", service);
    }

    /**
     * Records cache operation metrics for performance monitoring.
     * 
     * @param operation cache operation (hit, miss, eviction)
     * @param cacheName name of the cache
     */
    public void recordCacheOperation(String operation, String cacheName) {
        incrementCounter(PERFORMANCE_METRIC_PREFIX + ".cache." + operation, 
            "cache", cacheName);
        
        log.debug("Cache {} operation recorded for cache: {}", operation, cacheName);
    }

    // ========== BUSINESS METRICS ==========

    /**
     * Records business operation metrics for analytics and monitoring.
     * 
     * @param operation business operation name
     * @param value metric value
     * @param dimensions additional business dimensions
     */
    public void recordBusinessMetric(String operation, double value, Map<String, String> dimensions) {
        String[] tags = dimensions.entrySet().stream()
            .flatMap(entry -> Arrays.stream(new String[]{entry.getKey(), entry.getValue()}))
            .toArray(String[]::new);
        
        recordSummary(BUSINESS_METRIC_PREFIX + "." + operation, value, tags);
        log.info("Business metric recorded: {} value: {} dimensions: {}", operation, value, dimensions);
    }

    /**
     * Records user engagement metrics for analytics.
     * 
     * @param engagementType type of engagement (profile_view, settings_change, etc.)
     * @param userId user identifier
     * @param sessionId session identifier
     */
    public void recordUserEngagement(String engagementType, String userId, String sessionId) {
        incrementCounter(BUSINESS_METRIC_PREFIX + ".user.engagement", 
            "type", engagementType, "user_id", userId, "session_id", sessionId);
        
        log.debug("User engagement recorded: {} user: {} session: {}", engagementType, userId, sessionId);
    }

    // ========== SYSTEM METRICS ==========

    /**
     * Records active session count as a gauge metric.
     * 
     * @param sessionCounter session counter supplier
     */
    public void recordActiveSessionCount(AtomicLong sessionCounter) {
        recordGauge(SYSTEM_METRIC_PREFIX + ".sessions.active", sessionCounter::get);
    }

    /**
     * Records thread pool metrics for monitoring resource utilization.
     * 
     * @param poolName thread pool name
     * @param activeCount current active thread count
     * @param queueSize current queue size
     */
    public void recordThreadPoolMetrics(String poolName, int activeCount, int queueSize) {
        recordGauge(SYSTEM_METRIC_PREFIX + ".threadpool.active", 
            () -> activeCount, "pool", poolName);
        recordGauge(SYSTEM_METRIC_PREFIX + ".threadpool.queue_size", 
            () -> queueSize, "pool", poolName);
        
        log.debug("Thread pool metrics recorded: {} active: {} queue: {}", poolName, activeCount, queueSize);
    }

    /**
     * Records memory usage metrics for monitoring and alerting.
     * 
     * @param memoryType type of memory (heap, non_heap)
     * @param usedBytes used memory in bytes
     * @param maxBytes maximum memory in bytes
     */
    public void recordMemoryUsage(String memoryType, long usedBytes, long maxBytes) {
        recordGauge(SYSTEM_METRIC_PREFIX + ".memory.used", 
            () -> usedBytes, "type", memoryType);
        recordGauge(SYSTEM_METRIC_PREFIX + ".memory.max", 
            () -> maxBytes, "type", memoryType);
        recordGauge(SYSTEM_METRIC_PREFIX + ".memory.usage_percent", 
            () -> (double) usedBytes / maxBytes * 100, "type", memoryType);
        
        log.debug("Memory usage recorded: {} used: {} max: {} usage: {}%", 
            memoryType, usedBytes, maxBytes, (double) usedBytes / maxBytes * 100);
    }

    // ========== UTILITY METHODS ==========

    /**
     * Builds a cache key for metric caching.
     * 
     * @param metricName metric name
     * @param tags metric tags
     * @return String cache key
     */
    private String buildCacheKey(String metricName, String... tags) {
        StringBuilder keyBuilder = new StringBuilder(metricName);
        for (String tag : tags) {
            keyBuilder.append(":").append(tag);
        }
        return keyBuilder.toString();
    }

    /**
     * Gets current metric registry for direct access.
     * 
     * @return MeterRegistry current meter registry
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    /**
     * Clears the metric cache (useful for testing).
     */
    public void clearCache() {
        counterCache.clear();
        timerCache.clear();
        gaugeCache.clear();
        summaryCache.clear();
        log.info("Metric cache cleared");
    }
}
