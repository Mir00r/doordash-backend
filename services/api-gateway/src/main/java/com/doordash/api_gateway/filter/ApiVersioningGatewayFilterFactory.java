package com.doordash.api_gateway.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * API versioning filter that supports multiple versioning strategies:
 * 1. Header-based versioning (X-API-Version)
 * 2. Path-based versioning (/api/v1/, /api/v2/)
 * 3. Query parameter versioning (?version=v1)
 */
@Component
@Slf4j
public class ApiVersioningGatewayFilterFactory 
    extends AbstractGatewayFilterFactory<ApiVersioningGatewayFilterFactory.Config> {

    private static final String VERSION_HEADER = "X-API-Version";
    private static final String VERSION_PARAM = "version";
    private static final String DEFAULT_VERSION = "v1";
    private static final List<String> SUPPORTED_VERSIONS = Arrays.asList("v1", "v2", "v3");

    public ApiVersioningGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String detectedVersion = detectApiVersion(request);
            
            // Validate version
            if (!SUPPORTED_VERSIONS.contains(detectedVersion)) {
                log.warn("Unsupported API version requested: {}. Using default version: {}", 
                    detectedVersion, DEFAULT_VERSION);
                detectedVersion = DEFAULT_VERSION;
            }
            
            // Add version headers for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                .header(VERSION_HEADER, detectedVersion)
                .header("X-API-Version-Strategy", getVersionStrategy(request))
                .header("X-Gateway-Version", "1.0")
                .build();
            
            ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
            
            log.debug("API Version resolved: {} for path: {}", detectedVersion, request.getPath());
            
            return chain.filter(modifiedExchange);
        };
    }

    /**
     * Detect API version using multiple strategies in order of precedence:
     * 1. Header-based (X-API-Version)
     * 2. Path-based (/api/v1/)
     * 3. Query parameter (?version=v1)
     * 4. Default version
     */
    private String detectApiVersion(ServerHttpRequest request) {
        // Strategy 1: Header-based versioning
        String headerVersion = request.getHeaders().getFirst(VERSION_HEADER);
        if (StringUtils.hasText(headerVersion)) {
            return normalizeVersion(headerVersion);
        }
        
        // Strategy 2: Path-based versioning
        String pathVersion = extractVersionFromPath(request.getPath().value());
        if (StringUtils.hasText(pathVersion)) {
            return pathVersion;
        }
        
        // Strategy 3: Query parameter versioning
        String queryVersion = request.getQueryParams().getFirst(VERSION_PARAM);
        if (StringUtils.hasText(queryVersion)) {
            return normalizeVersion(queryVersion);
        }
        
        // Strategy 4: Default version
        return DEFAULT_VERSION;
    }
    
    /**
     * Extract version from URL path (e.g., /api/v1/users -> v1)
     */
    private String extractVersionFromPath(String path) {
        if (path.matches(".*\\/api\\/v\\d+\\/.*")) {
            String[] pathSegments = path.split("/");
            for (String segment : pathSegments) {
                if (segment.matches("v\\d+")) {
                    return segment;
                }
            }
        }
        return null;
    }
    
    /**
     * Normalize version string (remove 'v' prefix if present and add it back)
     */
    private String normalizeVersion(String version) {
        if (version == null) {
            return DEFAULT_VERSION;
        }
        
        String normalized = version.toLowerCase().trim();
        
        // Remove 'v' prefix if present
        if (normalized.startsWith("v")) {
            normalized = normalized.substring(1);
        }
        
        // Validate it's a number
        if (normalized.matches("\\d+")) {
            return "v" + normalized;
        }
        
        return DEFAULT_VERSION;
    }
    
    /**
     * Determine which versioning strategy was used
     */
    private String getVersionStrategy(ServerHttpRequest request) {
        if (StringUtils.hasText(request.getHeaders().getFirst(VERSION_HEADER))) {
            return "header";
        }
        if (extractVersionFromPath(request.getPath().value()) != null) {
            return "path";
        }
        if (StringUtils.hasText(request.getQueryParams().getFirst(VERSION_PARAM))) {
            return "query";
        }
        return "default";
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("enabled", "defaultVersion", "supportedVersions");
    }

    @Data
    public static class Config {
        private boolean enabled = true;
        private String defaultVersion = DEFAULT_VERSION;
        private List<String> supportedVersions = SUPPORTED_VERSIONS;
    }
}
