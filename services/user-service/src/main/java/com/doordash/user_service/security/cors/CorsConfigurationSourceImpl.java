package com.doordash.user_service.security.cors;

import com.doordash.user_service.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.List;

/**
 * CORS Configuration Source Implementation for DoorDash User Service.
 * 
 * Implements comprehensive Cross-Origin Resource Sharing (CORS) configuration
 * for secure microservices communication and frontend integration.
 * 
 * Features:
 * - Dynamic CORS policy based on request context
 * - Environment-specific origin configuration
 * - Service-to-service communication support
 * - Frontend application integration
 * - Security-conscious default policies
 * - Preflight request optimization
 * - Credential handling for authenticated requests
 * - Custom header support for API communication
 * 
 * Security Considerations:
 * - Restrictive origin policies for production
 * - Proper credential handling
 * - Limited exposed headers
 * - Appropriate preflight caching
 * - Protection against CSRF attacks
 * - Integration with API Gateway routing
 * 
 * Microservices Integration:
 * - API Gateway origin allowance
 * - Inter-service communication support
 * - Load balancer compatibility
 * - Development environment flexibility
 * - Production security enforcement
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@RequiredArgsConstructor
@Slf4j
public class CorsConfigurationSourceImpl implements CorsConfigurationSource {

    private final SecurityProperties securityProperties;

    /**
     * Retrieves CORS configuration for the given request.
     * 
     * Provides dynamic CORS configuration based on:
     * - Request origin and headers
     * - Environment configuration
     * - Service authentication context
     * - Security policy requirements
     * 
     * @param request the HTTP request
     * @return CorsConfiguration the CORS configuration for the request
     */
    @Override
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        try {
            CorsConfiguration config = new CorsConfiguration();
            SecurityProperties.Cors corsConfig = securityProperties.getCors();
            
            // Configure allowed origins
            configureAllowedOrigins(config, corsConfig, request);
            
            // Configure allowed methods
            configureAllowedMethods(config, corsConfig);
            
            // Configure allowed headers
            configureAllowedHeaders(config, corsConfig, request);
            
            // Configure exposed headers
            configureExposedHeaders(config, corsConfig);
            
            // Configure credentials support
            configureCredentials(config, corsConfig, request);
            
            // Configure preflight settings
            configurePreflight(config, corsConfig);
            
            // Apply security validations
            applySecurityValidations(config, request);
            
            log.debug("CORS configuration generated for origin: {}", request.getHeader("Origin"));
            
            return config;
            
        } catch (Exception e) {
            log.error("Failed to generate CORS configuration: {}", e.getMessage(), e);
            
            // Return restrictive configuration on error
            return createRestrictiveCorsConfiguration();
        }
    }

    /**
     * Configures allowed origins based on environment and request context.
     * 
     * @param config the CORS configuration
     * @param corsConfig the CORS properties
     * @param request the HTTP request
     */
    private void configureAllowedOrigins(
            CorsConfiguration config, 
            SecurityProperties.Cors corsConfig, 
            HttpServletRequest request) {
        
        List<String> allowedOrigins = corsConfig.getAllowedOrigins();
        String requestOrigin = request.getHeader("Origin");
        
        // In development, be more permissive
        if (isDevelopmentEnvironment()) {
            config.setAllowedOriginPatterns(List.of("*"));
            log.debug("Development environment: allowing all origins");
            return;
        }
        
        // In production, use strict origin validation
        if (isValidOrigin(requestOrigin, allowedOrigins)) {
            config.setAllowedOrigins(allowedOrigins);
        } else {
            // If origin is not in allowed list, apply restrictive policy
            config.setAllowedOrigins(List.of());
            log.warn("Origin not in allowed list: {}", requestOrigin);
        }
    }

    /**
     * Configures allowed HTTP methods.
     * 
     * @param config the CORS configuration
     * @param corsConfig the CORS properties
     */
    private void configureAllowedMethods(CorsConfiguration config, SecurityProperties.Cors corsConfig) {
        List<String> allowedMethods = corsConfig.getAllowedMethods();
        config.setAllowedMethods(allowedMethods);
        
        log.debug("Configured allowed methods: {}", allowedMethods);
    }

    /**
     * Configures allowed headers with dynamic additions based on request context.
     * 
     * @param config the CORS configuration
     * @param corsConfig the CORS properties
     * @param request the HTTP request
     */
    private void configureAllowedHeaders(
            CorsConfiguration config, 
            SecurityProperties.Cors corsConfig, 
            HttpServletRequest request) {
        
        List<String> allowedHeaders = corsConfig.getAllowedHeaders();
        config.setAllowedHeaders(allowedHeaders);
        
        // Add service-specific headers for microservices communication
        if (isServiceToServiceRequest(request)) {
            config.addAllowedHeader("X-Service-Name");
            config.addAllowedHeader("X-Service-Version");
            config.addAllowedHeader("X-Correlation-ID");
            config.addAllowedHeader("X-Request-ID");
            log.debug("Added service-to-service headers");
        }
        
        // Add API Gateway headers if request comes from gateway
        if (isApiGatewayRequest(request)) {
            config.addAllowedHeader("X-Gateway-Route");
            config.addAllowedHeader("X-Gateway-Version");
            log.debug("Added API Gateway headers");
        }
    }

    /**
     * Configures headers exposed to the client.
     * 
     * @param config the CORS configuration
     * @param corsConfig the CORS properties
     */
    private void configureExposedHeaders(CorsConfiguration config, SecurityProperties.Cors corsConfig) {
        List<String> exposedHeaders = corsConfig.getExposedHeaders();
        if (exposedHeaders != null && !exposedHeaders.isEmpty()) {
            config.setExposedHeaders(exposedHeaders);
        }
        
        // Always expose standard API headers
        config.addExposedHeader("X-Total-Count");
        config.addExposedHeader("X-Rate-Limit-Remaining");
        config.addExposedHeader("X-Rate-Limit-Reset");
        config.addExposedHeader("X-Request-ID");
    }

    /**
     * Configures credential support based on security requirements.
     * 
     * @param config the CORS configuration
     * @param corsConfig the CORS properties
     * @param request the HTTP request
     */
    private void configureCredentials(
            CorsConfiguration config, 
            SecurityProperties.Cors corsConfig, 
            HttpServletRequest request) {
        
        boolean allowCredentials = corsConfig.isAllowCredentials();
        
        // Only allow credentials for trusted origins
        if (allowCredentials && isTrustedOrigin(request.getHeader("Origin"))) {
            config.setAllowCredentials(true);
            log.debug("Credentials allowed for trusted origin");
        } else {
            config.setAllowCredentials(false);
            if (allowCredentials) {
                log.debug("Credentials denied for untrusted origin: {}", request.getHeader("Origin"));
            }
        }
    }

    /**
     * Configures preflight request settings.
     * 
     * @param config the CORS configuration
     * @param corsConfig the CORS properties
     */
    private void configurePreflight(CorsConfiguration config, SecurityProperties.Cors corsConfig) {
        Duration maxAge = corsConfig.getMaxAge();
        if (maxAge != null) {
            config.setMaxAge(maxAge.toSeconds());
        }
        
        log.debug("Preflight max age configured: {} seconds", maxAge != null ? maxAge.toSeconds() : "default");
    }

    /**
     * Applies additional security validations to the CORS configuration.
     * 
     * @param config the CORS configuration
     * @param request the HTTP request
     */
    private void applySecurityValidations(CorsConfiguration config, HttpServletRequest request) {
        // Validate that credentials are not allowed with wildcard origins
        if (config.getAllowCredentials() != null && config.getAllowCredentials()) {
            List<String> origins = config.getAllowedOrigins();
            List<String> originPatterns = config.getAllowedOriginPatterns();
            
            if ((origins != null && origins.contains("*")) || 
                (originPatterns != null && originPatterns.contains("*"))) {
                
                log.warn("Credentials not allowed with wildcard origins, disabling credentials");
                config.setAllowCredentials(false);
            }
        }
        
        // Apply path-specific restrictions
        applyPathSpecificRestrictions(config, request);
    }

    /**
     * Applies path-specific CORS restrictions.
     * 
     * @param config the CORS configuration
     * @param request the HTTP request
     */
    private void applyPathSpecificRestrictions(CorsConfiguration config, HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        
        // Restrict admin endpoints
        if (requestPath.startsWith("/api/v1/admin/")) {
            config.setAllowedOrigins(List.of("https://admin.doordash.com"));
            config.setAllowCredentials(true);
            log.debug("Applied admin endpoint restrictions");
        }
        
        // Restrict actuator endpoints
        if (requestPath.startsWith("/actuator/")) {
            config.setAllowedOrigins(List.of("https://monitoring.doordash.com"));
            config.setAllowCredentials(false);
            log.debug("Applied actuator endpoint restrictions");
        }
    }

    /**
     * Creates a restrictive CORS configuration for error scenarios.
     * 
     * @return CorsConfiguration the restrictive configuration
     */
    private CorsConfiguration createRestrictiveCorsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of());
        config.setAllowedMethods(List.of("GET"));
        config.setAllowedHeaders(List.of("Content-Type"));
        config.setAllowCredentials(false);
        config.setMaxAge(0L);
        
        log.warn("Applied restrictive CORS configuration due to error");
        
        return config;
    }

    /**
     * Validates if the origin is in the allowed origins list.
     * 
     * @param origin the request origin
     * @param allowedOrigins the list of allowed origins
     * @return boolean true if origin is valid
     */
    private boolean isValidOrigin(String origin, List<String> allowedOrigins) {
        if (origin == null || allowedOrigins == null) {
            return false;
        }
        
        return allowedOrigins.contains(origin) || 
               allowedOrigins.stream().anyMatch(allowed -> matchesOriginPattern(origin, allowed));
    }

    /**
     * Checks if origin matches the pattern (supports wildcards).
     * 
     * @param origin the request origin
     * @param pattern the origin pattern
     * @return boolean true if origin matches pattern
     */
    private boolean matchesOriginPattern(String origin, String pattern) {
        if ("*".equals(pattern)) {
            return true;
        }
        
        // Support simple wildcard patterns like *.doordash.com
        if (pattern.startsWith("*.")) {
            String domain = pattern.substring(2);
            return origin.endsWith("." + domain) || origin.equals(domain);
        }
        
        return origin.equals(pattern);
    }

    /**
     * Checks if the request is from a trusted origin.
     * 
     * @param origin the request origin
     * @return boolean true if origin is trusted
     */
    private boolean isTrustedOrigin(String origin) {
        if (origin == null) {
            return false;
        }
        
        List<String> trustedOrigins = List.of(
            "https://doordash.com",
            "https://admin.doordash.com",
            "https://api.doordash.com"
        );
        
        return trustedOrigins.contains(origin) || 
               origin.endsWith(".doordash.com");
    }

    /**
     * Checks if the request is a service-to-service communication.
     * 
     * @param request the HTTP request
     * @return boolean true if service-to-service request
     */
    private boolean isServiceToServiceRequest(HttpServletRequest request) {
        String serviceName = request.getHeader("X-Service-Name");
        String userAgent = request.getHeader("User-Agent");
        
        return serviceName != null || 
               (userAgent != null && userAgent.contains("DoorDash-Service"));
    }

    /**
     * Checks if the request comes from the API Gateway.
     * 
     * @param request the HTTP request
     * @return boolean true if API Gateway request
     */
    private boolean isApiGatewayRequest(HttpServletRequest request) {
        String gatewayHeader = request.getHeader("X-Gateway-Route");
        String forwardedBy = request.getHeader("X-Forwarded-By");
        
        return gatewayHeader != null || 
               (forwardedBy != null && forwardedBy.contains("api-gateway"));
    }

    /**
     * Checks if the application is running in development environment.
     * 
     * @return boolean true if development environment
     */
    private boolean isDevelopmentEnvironment() {
        String profile = System.getProperty("spring.profiles.active", "");
        return profile.contains("dev") || profile.contains("local");
    }
}
