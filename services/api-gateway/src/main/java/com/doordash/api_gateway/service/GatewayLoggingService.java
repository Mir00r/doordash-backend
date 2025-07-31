package com.doordash.api_gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for logging gateway requests, responses, and errors to Kafka.
 * Provides asynchronous logging capabilities for audit and monitoring.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GatewayLoggingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.gateway-logs:gateway-logs}")
    private String gatewayLogsTopic;

    @Value("${kafka.topics.security-events:security-events}")
    private String securityEventsTopic;

    @Value("${gateway.logging.enabled:true}")
    private boolean loggingEnabled;

    @Value("#{'${gateway.logging.sensitive-headers}'.split(',')}")
    private List<String> sensitiveHeaders;

    /**
     * Log incoming request details.
     */
    public void logRequest(String correlationId, String method, String uri, String clientIp, 
                          String userAgent, MultiValueMap<String, String> headers, LocalDateTime timestamp) {
        if (!loggingEnabled) {
            return;
        }

        try {
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("type", "REQUEST");
            logEntry.put("correlationId", correlationId);
            logEntry.put("method", method);
            logEntry.put("uri", uri);
            logEntry.put("clientIp", clientIp);
            logEntry.put("userAgent", userAgent);
            logEntry.put("headers", sanitizeHeaders(headers));
            logEntry.put("timestamp", timestamp);

            kafkaTemplate.send(gatewayLogsTopic, correlationId, logEntry);
            log.debug("Request logged to Kafka: {}", correlationId);
        } catch (Exception e) {
            log.warn("Failed to send request log to Kafka: {}", e.getMessage());
        }
    }

    /**
     * Log outgoing response details.
     */
    public void logResponse(String correlationId, int statusCode, long duration, 
                           MultiValueMap<String, String> headers, LocalDateTime timestamp) {
        if (!loggingEnabled) {
            return;
        }

        try {
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("type", "RESPONSE");
            logEntry.put("correlationId", correlationId);
            logEntry.put("statusCode", statusCode);
            logEntry.put("duration", duration);
            logEntry.put("headers", sanitizeHeaders(headers));
            logEntry.put("timestamp", timestamp);

            kafkaTemplate.send(gatewayLogsTopic, correlationId, logEntry);
            log.debug("Response logged to Kafka: {}", correlationId);
        } catch (Exception e) {
            log.warn("Failed to send response log to Kafka: {}", e.getMessage());
        }
    }

    /**
     * Log error details.
     */
    public void logError(String correlationId, String errorType, String errorMessage, 
                        long duration, LocalDateTime timestamp) {
        if (!loggingEnabled) {
            return;
        }

        try {
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("type", "ERROR");
            logEntry.put("correlationId", correlationId);
            logEntry.put("errorType", errorType);
            logEntry.put("errorMessage", errorMessage);
            logEntry.put("duration", duration);
            logEntry.put("timestamp", timestamp);

            kafkaTemplate.send(gatewayLogsTopic, correlationId, logEntry);
            log.debug("Error logged to Kafka: {}", correlationId);
        } catch (Exception e) {
            log.warn("Failed to send error log to Kafka: {}", e.getMessage());
        }
    }

    /**
     * Log security events (authentication failures, rate limiting, etc.)
     */
    public void logSecurityEvent(String correlationId, String eventType, String description, 
                                String clientIp, String userAgent, LocalDateTime timestamp) {
        try {
            Map<String, Object> securityEvent = new HashMap<>();
            securityEvent.put("type", "SECURITY_EVENT");
            securityEvent.put("correlationId", correlationId);
            securityEvent.put("eventType", eventType);
            securityEvent.put("description", description);
            securityEvent.put("clientIp", clientIp);
            securityEvent.put("userAgent", userAgent);
            securityEvent.put("timestamp", timestamp);

            kafkaTemplate.send(securityEventsTopic, correlationId, securityEvent);
            log.info("Security event logged: {} - {}", eventType, description);
        } catch (Exception e) {
            log.warn("Failed to send security event to Kafka: {}", e.getMessage());
        }
    }

    /**
     * Log rate limiting events.
     */
    public void logRateLimitEvent(String correlationId, String limitType, String key, 
                                 String action, String clientIp, LocalDateTime timestamp) {
        try {
            Map<String, Object> rateLimitEvent = new HashMap<>();
            rateLimitEvent.put("type", "RATE_LIMIT");
            rateLimitEvent.put("correlationId", correlationId);
            rateLimitEvent.put("limitType", limitType);
            rateLimitEvent.put("key", key);
            rateLimitEvent.put("action", action);
            rateLimitEvent.put("clientIp", clientIp);
            rateLimitEvent.put("timestamp", timestamp);

            kafkaTemplate.send(gatewayLogsTopic, correlationId, rateLimitEvent);
            log.debug("Rate limit event logged: {} - {}", action, key);
        } catch (Exception e) {
            log.warn("Failed to send rate limit event to Kafka: {}", e.getMessage());
        }
    }

    /**
     * Sanitize headers by removing sensitive information.
     */
    private Map<String, Object> sanitizeHeaders(MultiValueMap<String, String> headers) {
        Map<String, Object> sanitizedHeaders = new HashMap<>();
        
        if (headers != null) {
            headers.forEach((key, values) -> {
                if (isSensitiveHeader(key)) {
                    sanitizedHeaders.put(key, "[REDACTED]");
                } else {
                    sanitizedHeaders.put(key, values.size() == 1 ? values.get(0) : values);
                }
            });
        }
        
        return sanitizedHeaders;
    }

    /**
     * Check if a header contains sensitive information.
     */
    private boolean isSensitiveHeader(String headerName) {
        return sensitiveHeaders.stream()
            .anyMatch(sensitive -> headerName.toLowerCase().contains(sensitive.toLowerCase()));
    }
}
