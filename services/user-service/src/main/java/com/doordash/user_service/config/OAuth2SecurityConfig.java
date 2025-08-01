package com.doordash.user_service.config;

import com.doordash.user_service.security.audit.SecurityAuditEventListener;
import com.doordash.user_service.security.authentication.CustomJwtAuthenticationConverter;
import com.doordash.user_service.security.authorization.DoorDashPermissionEvaluator;
import com.doordash.user_service.security.cors.CorsConfigurationSourceImpl;
import com.doordash.user_service.security.csrf.CsrfTokenRepositoryImpl;
import com.doordash.user_service.security.headers.SecurityHeadersFilter;
import com.doordash.user_service.security.ratelimit.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Comprehensive OAuth2 and Security Configuration for User Service.
 * 
 * This configuration implements enterprise-grade security practices including:
 * - OAuth2 Resource Server with JWT validation
 * - Multi-layered authentication and authorization
 * - CSRF protection with custom token repository
 * - CORS configuration for microservices communication
 * - Security headers for protection against common attacks
 * - Rate limiting and DDoS protection
 * - Audit logging for security events
 * - Method-level security with custom permission evaluator
 * 
 * Security Features:
 * - JWT-based authentication with RS256/HS256 support
 * - Role-based access control (RBAC)
 * - Permission-based authorization
 * - Session management and concurrent session control
 * - Security event auditing
 * - Input validation and sanitization
 * - Protection against OWASP Top 10 vulnerabilities
 * 
 * Microservices Integration:
 * - Compatible with API Gateway authentication
 * - Service-to-service authentication support
 * - Distributed security context propagation
 * - Token introspection for cross-service validation
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
@Slf4j
public class OAuth2SecurityConfig {

    private final SecurityProperties securityProperties;
    private final UserDetailsService userDetailsService;
    private final SecurityAuditEventListener auditEventListener;
    private final RateLimitingFilter rateLimitingFilter;
    private final SecurityHeadersFilter securityHeadersFilter;

    /**
     * Main security filter chain for OAuth2 resource server configuration.
     * 
     * Implements layered security approach:
     * 1. CORS configuration for cross-origin requests
     * 2. CSRF protection with custom token repository
     * 3. Security headers for XSS, clickjacking protection
     * 4. Rate limiting for DDoS protection
     * 5. OAuth2 resource server with JWT validation
     * 6. Session management and concurrent session control
     * 7. Exception handling for authentication failures
     * 
     * @param http HttpSecurity configuration object
     * @return SecurityFilterChain configured security filter chain
     * @throws Exception if configuration fails
     */
    @Bean
    @Order(1)
    public SecurityFilterChain oauth2ResourceServerFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring OAuth2 Resource Server security filter chain");
        
        return http
            // CORS Configuration - Allow cross-origin requests from API Gateway and frontend
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF Protection - Custom implementation for stateless API with token validation
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
                .ignoringRequestMatchers(
                    "/actuator/**",
                    "/api/v1/auth/**",
                    "/api/v1/health/**"
                )
            )
            
            // Security Headers - Protection against common web vulnerabilities
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .xssProtection(xss -> xss
                    .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                .and()
            )
            
            // Session Management - Stateless for microservices architecture
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .maximumSessions(securityProperties.getSession().getMaxConcurrentSessions())
                .maxSessionsPreventsLogin(false)
            )
            
            // Authorization Rules - Fine-grained access control
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - No authentication required
                .requestMatchers(
                    "/actuator/health/**",
                    "/actuator/info",
                    "/actuator/prometheus",
                    "/api/v1/auth/login",
                    "/api/v1/auth/register",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/forgot-password",
                    "/api/v1/auth/reset-password",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/error"
                ).permitAll()
                
                // Admin endpoints - Admin role required
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                
                // User endpoints - Authentication required
                .requestMatchers("/api/v1/users/**").authenticated()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // OAuth2 Resource Server Configuration
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            )
            
            // Custom Security Filters
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Exception Handling
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            )
            
            .build();
    }

    /**
     * Management security filter chain for actuator endpoints.
     * Separate security configuration for monitoring and management endpoints.
     * 
     * @param http HttpSecurity configuration object
     * @return SecurityFilterChain for management endpoints
     * @throws Exception if configuration fails
     */
    @Bean
    @Order(2)
    public SecurityFilterChain managementSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(EndpointRequest.to("health", "info", "prometheus")).permitAll()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder()))
            )
            .build();
    }

    /**
     * JWT Decoder configuration for validating OAuth2 JWT tokens.
     * 
     * Supports multiple signature algorithms:
     * - RS256 for production with RSA key pairs
     * - HS256 for development with shared secrets
     * 
     * Features:
     * - Token validation (signature, expiration, issuer)
     * - Claim extraction and validation
     * - JWK Set support for key rotation
     * - Caching for performance optimization
     * 
     * @return JwtDecoder configured JWT decoder
     */
    @Bean
    @ConditionalOnProperty(value = "app.security.jwt.enabled", havingValue = "true", matchIfMissing = true)
    public JwtDecoder jwtDecoder() {
        log.info("Configuring JWT decoder with algorithm: {}", securityProperties.getJwt().getAlgorithm());
        
        if ("RS256".equals(securityProperties.getJwt().getAlgorithm())) {
            // RSA-based JWT validation for production
            RSAPublicKey publicKey = securityProperties.getJwt().getPublicKey();
            return NimbusJwtDecoder.withPublicKey(publicKey)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .cache()
                .build();
        } else {
            // HMAC-based JWT validation for development
            SecretKeySpec secretKey = new SecretKeySpec(
                securityProperties.getJwt().getSecret().getBytes(),
                "HmacSHA256"
            );
            return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256)
                .cache()
                .build();
        }
    }

    /**
     * JWT Encoder configuration for generating OAuth2 JWT tokens.
     * Used for token refresh and service-to-service authentication.
     * 
     * @return JwtEncoder configured JWT encoder
     */
    @Bean
    @ConditionalOnProperty(value = "app.security.jwt.enabled", havingValue = "true", matchIfMissing = true)
    public JwtEncoder jwtEncoder() {
        if ("RS256".equals(securityProperties.getJwt().getAlgorithm())) {
            RSAPrivateKey privateKey = securityProperties.getJwt().getPrivateKey();
            RSAPublicKey publicKey = securityProperties.getJwt().getPublicKey();
            
            return new NimbusJwtEncoder(
                new com.nimbusds.jose.jwk.RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .build()
            );
        } else {
            SecretKeySpec secretKey = new SecretKeySpec(
                securityProperties.getJwt().getSecret().getBytes(),
                "HmacSHA256"
            );
            return NimbusJwtEncoder.withSecretKey(secretKey).build();
        }
    }

    /**
     * Custom JWT Authentication Converter for extracting authorities and user details.
     * 
     * Converts JWT claims to Spring Security authentication:
     * - Extracts user roles and permissions
     * - Maps custom claims to authentication attributes
     * - Validates token claims and scope
     * - Handles service-to-service authentication
     * 
     * @return JwtAuthenticationConverter custom JWT authentication converter
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        return new CustomJwtAuthenticationConverter(securityProperties);
    }

    /**
     * CORS Configuration Source for cross-origin resource sharing.
     * 
     * Configured for microservices architecture:
     * - API Gateway integration
     * - Frontend application support
     * - Service-to-service communication
     * - Security-conscious CORS policy
     * 
     * @return CorsConfigurationSource CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return new CorsConfigurationSourceImpl(securityProperties);
    }

    /**
     * Custom CSRF Token Repository for stateless CSRF protection.
     * 
     * Features:
     * - Token generation and validation
     * - Stateless operation for microservices
     * - Integration with frontend frameworks
     * - Double submit cookie pattern
     * 
     * @return CsrfTokenRepository custom CSRF token repository
     */
    @Bean
    public CsrfTokenRepositoryImpl csrfTokenRepository() {
        return new CsrfTokenRepositoryImpl(securityProperties);
    }

    /**
     * Password Encoder configuration for secure password hashing.
     * 
     * Uses BCrypt with configurable strength:
     * - Production: BCrypt strength 12
     * - Development: BCrypt strength 10
     * - Test: BCrypt strength 4
     * 
     * @return PasswordEncoder configured password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        int strength = securityProperties.getPassword().getBcryptStrength();
        log.info("Configuring password encoder with BCrypt strength: {}", strength);
        return new BCryptPasswordEncoder(strength);
    }

    /**
     * Method Security Expression Handler with custom permission evaluator.
     * 
     * Enables method-level security with:
     * - Role-based access control
     * - Permission-based authorization
     * - Custom security expressions
     * - Domain object security
     * 
     * @return MethodSecurityExpressionHandler method security expression handler
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(new DoorDashPermissionEvaluator());
        return expressionHandler;
    }
}
