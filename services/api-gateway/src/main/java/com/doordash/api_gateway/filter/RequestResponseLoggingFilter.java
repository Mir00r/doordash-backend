package com.doordash.api_gateway.filter;

import com.doordash.api_gateway.service.GatewayLoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Global filter for request/response logging and correlation ID injection.
 * Logs all incoming requests and outgoing responses for audit and monitoring.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RequestResponseLoggingFilter implements GlobalFilter, Ordered {

    private final GatewayLoggingService loggingService;

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String REQUEST_START_TIME = "request_start_time";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Generate or extract correlation ID
        String correlationId = getOrGenerateCorrelationId(request);
        
        // Add correlation ID to request and response headers
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
            .header(CORRELATION_ID_HEADER, correlationId)
            .build();
        
        ServerHttpResponse modifiedResponse = exchange.getResponse().mutate()
            .header(CORRELATION_ID_HEADER, correlationId)
            .build();
        
        ServerWebExchange modifiedExchange = exchange.mutate()
            .request(modifiedRequest)
            .response(modifiedResponse)
            .build();
        
        // Store request start time
        modifiedExchange.getAttributes().put(REQUEST_START_TIME, System.currentTimeMillis());
        
        // Log incoming request
        logIncomingRequest(modifiedRequest, correlationId);
        
        return chain.filter(modifiedExchange)
            .doOnSuccess(aVoid -> logOutgoingResponse(modifiedExchange, correlationId))
            .doOnError(throwable -> logErrorResponse(modifiedExchange, correlationId, throwable));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    /**
     * Get existing correlation ID or generate a new one.
     */
    private String getOrGenerateCorrelationId(ServerHttpRequest request) {
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }

    /**
     * Log incoming request details.
     */
    private void logIncomingRequest(ServerHttpRequest request, String correlationId) {
        try {
            String clientIp = getClientIpAddress(request);
            String userAgent = request.getHeaders().getFirst("User-Agent");
            
            log.info("Incoming Request [{}] - Method: {}, URI: {}, IP: {}, User-Agent: {}",
                correlationId,
                request.getMethod(),
                request.getURI(),
                clientIp,
                userAgent);
            
            // Async logging to Kafka
            loggingService.logRequest(
                correlationId,
                request.getMethod().name(),
                request.getURI().toString(),
                clientIp,
                userAgent,
                request.getHeaders(),
                LocalDateTime.now()
            );
        } catch (Exception e) {
            log.warn("Failed to log incoming request: {}", e.getMessage());
        }
    }

    /**
     * Log outgoing response details.
     */
    private void logOutgoingResponse(ServerWebExchange exchange, String correlationId) {
        try {
            ServerHttpResponse response = exchange.getResponse();
            Long startTime = exchange.getAttribute(REQUEST_START_TIME);
            long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
            
            log.info("Outgoing Response [{}] - Status: {}, Duration: {}ms",
                correlationId,
                response.getStatusCode(),
                duration);
            
            // Async logging to Kafka
            loggingService.logResponse(
                correlationId,
                response.getStatusCode() != null ? response.getStatusCode().value() : 0,
                duration,
                response.getHeaders(),
                LocalDateTime.now()
            );
        } catch (Exception e) {
            log.warn("Failed to log outgoing response: {}", e.getMessage());
        }
    }

    /**
     * Log error response details.
     */
    private void logErrorResponse(ServerWebExchange exchange, String correlationId, Throwable throwable) {
        try {
            Long startTime = exchange.getAttribute(REQUEST_START_TIME);
            long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
            
            log.error("Error Response [{}] - Error: {}, Duration: {}ms",
                correlationId,
                throwable.getMessage(),
                duration,
                throwable);
            
            // Async error logging to Kafka
            loggingService.logError(
                correlationId,
                throwable.getClass().getSimpleName(),
                throwable.getMessage(),
                duration,
                LocalDateTime.now()
            );
        } catch (Exception e) {
            log.warn("Failed to log error response: {}", e.getMessage());
        }
    }

    /**
     * Extract client IP address from request headers and remote address.
     */
    private String getClientIpAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.trim().isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null 
            ? request.getRemoteAddress().getAddress().getHostAddress() 
            : "unknown";
    }
}
