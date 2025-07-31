package com.doordash.api_gateway.filter;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Circuit breaker filter for resilience and fault tolerance.
 * Implements the circuit breaker pattern to prevent cascade failures.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CircuitBreakerGatewayFilterFactory 
    extends AbstractGatewayFilterFactory<CircuitBreakerGatewayFilterFactory.Config> {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String serviceName = config.getName();
            if (serviceName == null) {
                // Extract service name from route ID or path
                serviceName = extractServiceName(exchange);
            }
            
            CircuitBreaker circuitBreaker = circuitBreakerRegistry
                .circuitBreaker(serviceName, config.getName());
            
            log.debug("Applying circuit breaker for service: {}, state: {}", 
                serviceName, circuitBreaker.getState());
            
            return chain.filter(exchange)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(Exception.class, throwable -> {
                    log.error("Circuit breaker triggered for service: {}, error: {}", 
                        serviceName, throwable.getMessage());
                    
                    if (config.getFallbackUri() != null) {
                        return handleFallback(exchange, config.getFallbackUri());
                    }
                    
                    return Mono.error(new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE, 
                        "Service temporarily unavailable"));
                });
        };
    }

    /**
     * Extract service name from exchange context
     */
    private String extractServiceName(org.springframework.web.server.ServerWebExchange exchange) {
        // Try to get from route attributes
        String routeId = exchange.getAttribute("org.springframework.cloud.gateway.support.RouteDefinitionRouteLocator.routeDefinition");
        if (routeId != null) {
            return routeId;
        }
        
        // Extract from path
        String path = exchange.getRequest().getPath().value();
        if (path.startsWith("/api/v")) {
            String[] segments = path.split("/");
            if (segments.length > 3) {
                return segments[3] + "-service";
            }
        }
        
        return "default-service";
    }

    /**
     * Handle fallback response
     */
    private Mono<Void> handleFallback(org.springframework.web.server.ServerWebExchange exchange, String fallbackUri) {
        log.info("Executing fallback for request: {}", exchange.getRequest().getPath());
        
        // For now, return a standard circuit breaker response
        // In a real implementation, you might redirect to a fallback service
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        
        String fallbackResponse = "{\"error\":\"Service temporarily unavailable\",\"message\":\"Please try again later\"}";
        org.springframework.core.io.buffer.DataBuffer buffer = exchange.getResponse()
            .bufferFactory().wrap(fallbackResponse.getBytes());
        
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("name", "fallbackUri");
    }

    @Data
    public static class Config {
        private String name;
        private String fallbackUri;
    }
}
