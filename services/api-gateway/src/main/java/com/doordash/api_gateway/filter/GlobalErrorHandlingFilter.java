package com.doordash.api_gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global error handling filter for standardized error responses across the API Gateway.
 * Catches and formats all errors with consistent error structure.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GlobalErrorHandlingFilter implements GlobalFilter, Ordered {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
            .onErrorResume(Throwable.class, throwable -> handleError(exchange, throwable));
    }
    
    private Mono<Void> handleError(ServerWebExchange exchange, Throwable throwable) {
        ServerHttpResponse response = exchange.getResponse();
        
        // Set default status code
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorCode = "INTERNAL_SERVER_ERROR";
        String message = "An unexpected error occurred";
        
        // Determine specific error type and status
        if (throwable instanceof ResponseStatusException) {
            ResponseStatusException ex = (ResponseStatusException) throwable;
            status = ex.getStatus();
            message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
            errorCode = mapStatusToErrorCode(status);
        } else if (throwable instanceof java.net.ConnectException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorCode = "SERVICE_UNAVAILABLE";
            message = "Service temporarily unavailable";
        } else if (throwable instanceof java.util.concurrent.TimeoutException) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            errorCode = "GATEWAY_TIMEOUT";
            message = "Request timeout";
        } else if (throwable instanceof org.springframework.web.server.ServerWebInputException) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = "BAD_REQUEST";
            message = "Invalid request format";
        }
        
        // Log the error
        log.error("API Gateway Error - Path: {}, Method: {}, Status: {}, Error: {}", 
            exchange.getRequest().getPath(),
            exchange.getRequest().getMethod(),
            status,
            throwable.getMessage(),
            throwable);
        
        // Create standardized error response
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .errorCode(errorCode)
            .message(message)
            .path(exchange.getRequest().getPath().value())
            .method(exchange.getRequest().getMethod().name())
            .requestId(getRequestId(exchange))
            .build();
        
        return writeErrorResponse(response, errorResponse, status);
    }
    
    private String mapStatusToErrorCode(HttpStatus status) {
        switch (status) {
            case BAD_REQUEST: return "BAD_REQUEST";
            case UNAUTHORIZED: return "UNAUTHORIZED";
            case FORBIDDEN: return "FORBIDDEN";
            case NOT_FOUND: return "NOT_FOUND";
            case METHOD_NOT_ALLOWED: return "METHOD_NOT_ALLOWED";
            case TOO_MANY_REQUESTS: return "RATE_LIMIT_EXCEEDED";
            case INTERNAL_SERVER_ERROR: return "INTERNAL_SERVER_ERROR";
            case BAD_GATEWAY: return "BAD_GATEWAY";
            case SERVICE_UNAVAILABLE: return "SERVICE_UNAVAILABLE";
            case GATEWAY_TIMEOUT: return "GATEWAY_TIMEOUT";
            default: return "UNKNOWN_ERROR";
        }
    }
    
    private String getRequestId(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst("X-Request-ID");
    }
    
    private Mono<Void> writeErrorResponse(ServerHttpResponse response, ErrorResponse errorResponse, HttpStatus status) {
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        try {
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response", e);
            String fallbackResponse = "{\"error\":\"Internal server error\",\"message\":\"Failed to process error response\"}";
            DataBuffer buffer = response.bufferFactory().wrap(fallbackResponse.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
    }
    
    @Override
    public int getOrder() {
        return -1; // Execute after all other filters
    }
    
    /**
     * Standardized error response structure
     */
    @lombok.Data
    @lombok.Builder
    private static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String errorCode;
        private String message;
        private String path;
        private String method;
        private String requestId;
        private Map<String, Object> details = new HashMap<>();
    }
}
