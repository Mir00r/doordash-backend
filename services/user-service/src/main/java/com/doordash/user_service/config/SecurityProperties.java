package com.doordash.user_service.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * Comprehensive Security Configuration Properties for DoorDash User Service.
 * 
 * This configuration class centralizes all security-related settings including:
 * - OAuth2 and JWT configuration
 * - CORS and CSRF settings
 * - Rate limiting parameters
 * - Session management
 * - Password policies
 * - Audit and logging configuration
 * - Security headers and protection settings
 * 
 * Features:
 * - Type-safe configuration with validation
 * - Environment-specific settings (dev, staging, prod)
 * - Hot-reload support for dynamic configuration
 * - Integration with Spring Boot's configuration properties
 * - Comprehensive validation and error handling
 * - Documentation for all security parameters
 * 
 * Security Considerations:
 * - Sensitive values should be externalized
 * - Production values should use environment variables
 * - Default values are secure by default
 * - Validation ensures configuration integrity
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@Configuration
@ConfigurationProperties(prefix = "app.security")
@Validated
@Data
public class SecurityProperties {

    /**
     * JWT (JSON Web Token) configuration for OAuth2 authentication.
     * 
     * Supports both RSA (RS256) and HMAC (HS256) algorithms:
     * - RS256: Production use with public/private key pairs
     * - HS256: Development use with shared secrets
     */
    @Valid
    @NotNull
    private Jwt jwt = new Jwt();

    /**
     * CORS (Cross-Origin Resource Sharing) configuration.
     * Configured for microservices architecture with API Gateway.
     */
    @Valid
    @NotNull
    private Cors cors = new Cors();

    /**
     * CSRF (Cross-Site Request Forgery) protection configuration.
     * Implements double submit cookie pattern for stateless protection.
     */
    @Valid
    @NotNull
    private Csrf csrf = new Csrf();

    /**
     * Rate limiting configuration for DDoS protection and API throttling.
     * Implements token bucket algorithm with configurable limits.
     */
    @Valid
    @NotNull
    private RateLimit rateLimit = new RateLimit();

    /**
     * Session management configuration for concurrent session control.
     * Stateless by default for microservices architecture.
     */
    @Valid
    @NotNull
    private Session session = new Session();

    /**
     * Password policy configuration for secure authentication.
     * Enforces strong password requirements and secure hashing.
     */
    @Valid
    @NotNull
    private Password password = new Password();

    /**
     * Security audit and logging configuration.
     * Tracks security events for compliance and monitoring.
     */
    @Valid
    @NotNull
    private Audit audit = new Audit();

    /**
     * Security headers configuration for web protection.
     * Implements OWASP security header recommendations.
     */
    @Valid
    @NotNull
    private Headers headers = new Headers();

    /**
     * OAuth2 and service-to-service authentication configuration.
     * Supports multiple OAuth2 flows and microservices integration.
     */
    @Valid
    @NotNull
    private OAuth2 oauth2 = new OAuth2();

    /**
     * JWT Configuration Properties.
     * 
     * Configures JSON Web Token generation and validation:
     * - Algorithm selection (RS256/HS256)
     * - Key management (RSA keys/shared secrets)
     * - Token lifecycle (expiration, refresh)
     * - Claim configuration and validation
     */
    @Data
    public static class Jwt {
        
        /**
         * JWT signing algorithm (RS256 for production, HS256 for development).
         */
        @NotBlank
        @Pattern(regexp = "^(RS256|HS256)$", message = "JWT algorithm must be RS256 or HS256")
        private String algorithm = "RS256";

        /**
         * JWT secret for HMAC signing (HS256 algorithm).
         * Must be at least 256 bits (32 characters) for security.
         */
        @Size(min = 32, message = "JWT secret must be at least 32 characters")
        private String secret;

        /**
         * RSA public key for JWT verification (RS256 algorithm).
         */
        private RSAPublicKey publicKey;

        /**
         * RSA private key for JWT signing (RS256 algorithm).
         */
        private RSAPrivateKey privateKey;

        /**
         * JWT access token expiration time.
         */
        @NotNull
        @Positive
        private Duration accessTokenExpiration = Duration.ofMinutes(15);

        /**
         * JWT refresh token expiration time.
         */
        @NotNull
        @Positive
        private Duration refreshTokenExpiration = Duration.ofDays(7);

        /**
         * JWT issuer claim for token validation.
         */
        @NotBlank
        private String issuer = "doordash-user-service";

        /**
         * JWT audience claim for token validation.
         */
        @NotBlank
        private String audience = "doordash-api";

        /**
         * Enable JWT token caching for performance.
         */
        private boolean cacheEnabled = true;

        /**
         * JWT cache size for token validation.
         */
        @Positive
        private int cacheSize = 1000;

        /**
         * JWT cache TTL (Time To Live).
         */
        @NotNull
        @Positive
        private Duration cacheTtl = Duration.ofMinutes(5);
    }

    /**
     * CORS Configuration Properties.
     * 
     * Configures Cross-Origin Resource Sharing for web security:
     * - Allowed origins (API Gateway, frontend)
     * - Allowed methods and headers
     * - Credentials support
     * - Preflight request handling
     */
    @Data
    public static class Cors {
        
        /**
         * Allowed CORS origins. Use specific URLs in production.
         */
        @NotEmpty
        private List<String> allowedOrigins = List.of(
            "http://localhost:3000",    // React frontend
            "http://localhost:8080",    // API Gateway
            "https://doordash.com",     // Production frontend
            "https://api.doordash.com"  // Production API Gateway
        );

        /**
         * Allowed HTTP methods for CORS requests.
         */
        @NotEmpty
        private List<String> allowedMethods = List.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        );

        /**
         * Allowed headers for CORS requests.
         */
        @NotEmpty
        private List<String> allowedHeaders = List.of(
            "Authorization", "Content-Type", "X-Requested-With", 
            "X-CSRF-Token", "X-Forwarded-For", "X-Real-IP"
        );

        /**
         * Headers exposed to the client.
         */
        private List<String> exposedHeaders = List.of(
            "X-Total-Count", "X-Rate-Limit-Remaining", "X-Rate-Limit-Reset"
        );

        /**
         * Allow credentials in CORS requests (cookies, authorization headers).
         */
        private boolean allowCredentials = true;

        /**
         * Preflight request cache duration.
         */
        @NotNull
        @Positive
        private Duration maxAge = Duration.ofHours(1);
    }

    /**
     * CSRF Configuration Properties.
     * 
     * Configures Cross-Site Request Forgery protection:
     * - Token generation and validation
     * - Cookie-based token storage
     * - Stateless operation support
     * - Double submit cookie pattern
     */
    @Data
    public static class Csrf {
        
        /**
         * Enable CSRF protection.
         */
        private boolean enabled = true;

        /**
         * CSRF token cookie name.
         */
        @NotBlank
        private String cookieName = "XSRF-TOKEN";

        /**
         * CSRF token header name.
         */
        @NotBlank
        private String headerName = "X-XSRF-TOKEN";

        /**
         * CSRF token parameter name for form submissions.
         */
        @NotBlank
        private String parameterName = "_csrf";

        /**
         * CSRF cookie HTTP-only flag.
         */
        private boolean httpOnly = false;

        /**
         * CSRF cookie secure flag (HTTPS only).
         */
        private boolean secure = true;

        /**
         * CSRF cookie SameSite attribute.
         */
        @NotBlank
        private String sameSite = "Strict";

        /**
         * CSRF token expiration time.
         */
        @NotNull
        @Positive
        private Duration tokenExpiration = Duration.ofHours(1);
    }

    /**
     * Rate Limiting Configuration Properties.
     * 
     * Configures API rate limiting and DDoS protection:
     * - Token bucket algorithm
     * - Per-IP and per-user limits
     * - Sliding window implementation
     * - Burst capacity management
     */
    @Data
    public static class RateLimit {
        
        /**
         * Enable rate limiting.
         */
        private boolean enabled = true;

        /**
         * Rate limit algorithm (TOKEN_BUCKET, SLIDING_WINDOW).
         */
        @NotBlank
        @Pattern(regexp = "^(TOKEN_BUCKET|SLIDING_WINDOW)$")
        private String algorithm = "TOKEN_BUCKET";

        /**
         * Rate limit capacity (requests per window).
         */
        @Positive
        private int capacity = 100;

        /**
         * Rate limit refill rate (tokens per second).
         */
        @Positive
        private int refillRate = 10;

        /**
         * Rate limit time window duration.
         */
        @NotNull
        @Positive
        private Duration window = Duration.ofMinutes(1);

        /**
         * Burst capacity for handling traffic spikes.
         */
        @Positive
        private int burstCapacity = 200;

        /**
         * Rate limit key extraction strategy (IP, USER, API_KEY).
         */
        @NotBlank
        private String keyStrategy = "IP";

        /**
         * Endpoints excluded from rate limiting.
         */
        private Set<String> excludedPaths = Set.of(
            "/actuator/health",
            "/actuator/info"
        );
    }

    /**
     * Session Management Configuration Properties.
     * 
     * Configures HTTP session management:
     * - Concurrent session control
     * - Session timeout and cleanup
     * - Session fixation protection
     * - Stateless operation for APIs
     */
    @Data
    public static class Session {
        
        /**
         * Maximum concurrent sessions per user.
         */
        @Positive
        private int maxConcurrentSessions = 1;

        /**
         * Session timeout duration.
         */
        @NotNull
        @Positive
        private Duration timeout = Duration.ofMinutes(30);

        /**
         * Enable session fixation protection.
         */
        private boolean fixationProtection = true;

        /**
         * Session cookie name.
         */
        @NotBlank
        private String cookieName = "JSESSIONID";

        /**
         * Session cookie HTTP-only flag.
         */
        private boolean httpOnly = true;

        /**
         * Session cookie secure flag (HTTPS only).
         */
        private boolean secure = true;

        /**
         * Session cookie SameSite attribute.
         */
        @NotBlank
        private String sameSite = "Strict";
    }

    /**
     * Password Policy Configuration Properties.
     * 
     * Configures password security requirements:
     * - Complexity requirements
     * - BCrypt hashing configuration
     * - Password history and rotation
     * - Account lockout policies
     */
    @Data
    public static class Password {
        
        /**
         * Minimum password length.
         */
        @Min(8)
        @Max(128)
        private int minLength = 12;

        /**
         * Maximum password length.
         */
        @Min(8)
        @Max(256)
        private int maxLength = 128;

        /**
         * Require uppercase letters.
         */
        private boolean requireUppercase = true;

        /**
         * Require lowercase letters.
         */
        private boolean requireLowercase = true;

        /**
         * Require numeric digits.
         */
        private boolean requireDigits = true;

        /**
         * Require special characters.
         */
        private boolean requireSpecialChars = true;

        /**
         * BCrypt hashing strength (4-31, 12 recommended for production).
         */
        @Min(4)
        @Max(31)
        private int bcryptStrength = 12;

        /**
         * Password history size (prevent reuse).
         */
        @Min(0)
        @Max(24)
        private int historySize = 5;

        /**
         * Account lockout threshold (failed attempts).
         */
        @Positive
        private int lockoutThreshold = 5;

        /**
         * Account lockout duration.
         */
        @NotNull
        @Positive
        private Duration lockoutDuration = Duration.ofMinutes(15);
    }

    /**
     * Security Audit Configuration Properties.
     * 
     * Configures security event logging and auditing:
     * - Event types and filtering
     * - Log retention and rotation
     * - Compliance reporting
     * - Security monitoring integration
     */
    @Data
    public static class Audit {
        
        /**
         * Enable security audit logging.
         */
        private boolean enabled = true;

        /**
         * Audit log level (DEBUG, INFO, WARN, ERROR).
         */
        @NotBlank
        private String logLevel = "INFO";

        /**
         * Events to audit.
         */
        private Set<String> auditEvents = Set.of(
            "AUTHENTICATION_SUCCESS",
            "AUTHENTICATION_FAILURE",
            "AUTHORIZATION_FAILURE",
            "PASSWORD_CHANGE",
            "ACCOUNT_LOCKED",
            "PRIVILEGE_ESCALATION",
            "DATA_ACCESS",
            "CONFIGURATION_CHANGE"
        );

        /**
         * Include request details in audit logs.
         */
        private boolean includeRequestDetails = true;

        /**
         * Include response details in audit logs.
         */
        private boolean includeResponseDetails = false;

        /**
         * Audit log retention period.
         */
        @NotNull
        @Positive
        private Duration retentionPeriod = Duration.ofDays(90);
    }

    /**
     * Security Headers Configuration Properties.
     * 
     * Configures HTTP security headers:
     * - XSS protection
     * - Content type sniffing protection
     * - Clickjacking protection
     * - HSTS (HTTP Strict Transport Security)
     */
    @Data
    public static class Headers {
        
        /**
         * Enable security headers.
         */
        private boolean enabled = true;

        /**
         * X-Frame-Options header value.
         */
        @NotBlank
        private String frameOptions = "DENY";

        /**
         * X-Content-Type-Options header value.
         */
        @NotBlank
        private String contentTypeOptions = "nosniff";

        /**
         * X-XSS-Protection header value.
         */
        @NotBlank
        private String xssProtection = "1; mode=block";

        /**
         * Referrer-Policy header value.
         */
        @NotBlank
        private String referrerPolicy = "strict-origin-when-cross-origin";

        /**
         * Content-Security-Policy header value.
         */
        private String contentSecurityPolicy = "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'";

        /**
         * HTTP Strict Transport Security (HSTS) configuration.
         */
        private Hsts hsts = new Hsts();

        @Data
        public static class Hsts {
            /**
             * Enable HSTS.
             */
            private boolean enabled = true;

            /**
             * HSTS max age in seconds.
             */
            @Positive
            private long maxAge = 31536000; // 1 year

            /**
             * Include subdomains in HSTS.
             */
            private boolean includeSubdomains = true;

            /**
             * Enable HSTS preload.
             */
            private boolean preload = false;
        }
    }

    /**
     * OAuth2 Configuration Properties.
     * 
     * Configures OAuth2 flows and service integration:
     * - Client registration
     * - Authorization server settings
     * - Resource server configuration
     * - Service-to-service authentication
     */
    @Data
    public static class OAuth2 {
        
        /**
         * OAuth2 client configurations.
         */
        private List<Client> clients = List.of();

        /**
         * OAuth2 resource server settings.
         */
        private ResourceServer resourceServer = new ResourceServer();

        /**
         * Service-to-service authentication settings.
         */
        private ServiceAuth serviceAuth = new ServiceAuth();

        @Data
        public static class Client {
            @NotBlank
            private String clientId;
            
            @NotBlank
            private String clientSecret;
            
            @NotEmpty
            private Set<String> grantTypes = Set.of("authorization_code", "refresh_token");
            
            @NotEmpty
            private Set<String> scopes = Set.of("read", "write");
            
            @NotEmpty
            private Set<String> redirectUris = Set.of();
            
            @NotNull
            @Positive
            private Duration accessTokenValidity = Duration.ofMinutes(15);
            
            @NotNull
            @Positive
            private Duration refreshTokenValidity = Duration.ofDays(7);
        }

        @Data
        public static class ResourceServer {
            @NotBlank
            private String resourceId = "doordash-api";
            
            private boolean stateless = true;
            
            @NotNull
            @Positive
            private Duration tokenCheckInterval = Duration.ofMinutes(5);
        }

        @Data
        public static class ServiceAuth {
            @NotBlank
            private String clientId = "user-service";
            
            @NotBlank
            private String clientSecret;
            
            @NotEmpty
            private Set<String> trustedServices = Set.of(
                "api-gateway", "auth-service", "order-service", 
                "restaurant-service", "delivery-service"
            );
        }
    }
}
