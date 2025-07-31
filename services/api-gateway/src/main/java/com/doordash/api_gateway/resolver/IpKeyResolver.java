package com.doordash.api_gateway.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * Key resolver for rate limiting based on client IP address.
 * Used for anonymous requests and as a fallback mechanism.
 */
@Slf4j
public class IpKeyResolver implements KeyResolver {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";
    private static final String UNKNOWN_IP = "unknown";

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            String clientIp = getClientIpAddress(exchange);
            log.debug("Rate limiting key resolved for IP: {}", clientIp);
            return "ip:" + clientIp;
        });
    }

    /**
     * Extract client IP address from various headers and remote address.
     * Handles proxy scenarios with X-Forwarded-For and X-Real-IP headers.
     */
    private String getClientIpAddress(ServerWebExchange exchange) {
        // Check X-Forwarded-For header (most common proxy header)
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst(X_FORWARDED_FOR);
        if (isValidIp(xForwardedFor)) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            String[] ips = xForwardedFor.split(",");
            String firstIp = ips[0].trim();
            if (isValidIp(firstIp)) {
                return firstIp;
            }
        }

        // Check X-Real-IP header (nginx proxy)
        String xRealIp = exchange.getRequest().getHeaders().getFirst(X_REAL_IP);
        if (isValidIp(xRealIp)) {
            return xRealIp;
        }

        // Fall back to remote address
        return Optional.ofNullable(exchange.getRequest().getRemoteAddress())
            .map(InetSocketAddress::getAddress)
            .map(address -> address.getHostAddress())
            .filter(this::isValidIp)
            .orElse(UNKNOWN_IP);
    }

    /**
     * Validate if the IP address is valid and not a placeholder.
     */
    private boolean isValidIp(String ip) {
        return ip != null && 
               !ip.trim().isEmpty() && 
               !UNKNOWN_IP.equalsIgnoreCase(ip.trim()) &&
               !"127.0.0.1".equals(ip.trim()) &&
               !"0:0:0:0:0:0:0:1".equals(ip.trim()) &&
               !"::1".equals(ip.trim());
    }
}
