package com.doordash.user_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the User Service.
 * 
 * Configures JWT-based authentication, CORS settings, and endpoint security
 * for user profile management operations.
 * 
 * Features:
 * - JWT token validation and authentication
 * - Role-based access control (RBAC)
 * - CORS configuration for cross-origin requests
 * - Method-level security annotations
 * - Stateless session management
 * - Public health check endpoints
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Value("${app.security.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Value("${app.security.cors.allowed-methods}")
    private List<String> allowedMethods;

    @Value("${app.security.cors.allowed-headers}")
    private List<String> allowedHeaders;

    @Value("${app.security.cors.allow-credentials:true}")
    private boolean allowCredentials;

    /**
     * Configure the security filter chain.
     * 
     * @param http HttpSecurity configuration
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(HttpMethod.GET, "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/metrics").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll()
                        
                        // User profile endpoints - require authentication
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/profiles").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/profiles/me").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/profiles/{userId}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/profiles/{userId}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/profiles/{userId}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/profiles/{userId}/picture").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/profiles/{userId}/picture").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/profiles/{userId}/export").hasAnyRole("USER", "ADMIN")
                        
                        // Address endpoints - require authentication
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/{userId}/addresses").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/{userId}/addresses").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/{userId}/addresses/{addressId}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/{userId}/addresses/{addressId}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/{userId}/addresses/{addressId}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/{userId}/addresses/{addressId}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/{userId}/addresses/{addressId}/default").hasAnyRole("USER", "ADMIN")
                        
                        // Preferences endpoints - require authentication
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/{userId}/preferences").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/{userId}/preferences").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/{userId}/preferences").hasAnyRole("USER", "ADMIN")
                        
                        // Admin-only endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/profiles").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/profiles/search").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/profiles/{userId}/verification").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/profiles/{userId}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/profiles/statistics").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/addresses/analytics").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/activity-logs/**").hasRole("ADMIN")
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder()))
                )
                .build();
    }

    /**
     * Configure JWT decoder for token validation.
     * 
     * @return JwtDecoder instance
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec secretKey = new SecretKeySpec(
                jwtSecret.getBytes(), 
                "HmacSHA256"
        );
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    /**
     * Configure CORS settings.
     * 
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Set allowed origins
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            configuration.setAllowedOrigins(allowedOrigins);
        } else {
            configuration.setAllowedOrigins(Arrays.asList(
                    "http://localhost:3000",
                    "http://localhost:3001",
                    "https://app.doordash.com",
                    "https://admin.doordash.com"
            ));
        }
        
        // Set allowed methods
        if (allowedMethods != null && !allowedMethods.isEmpty()) {
            configuration.setAllowedMethods(allowedMethods);
        } else {
            configuration.setAllowedMethods(Arrays.asList(
                    "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
            ));
        }
        
        // Set allowed headers
        if (allowedHeaders != null && !allowedHeaders.isEmpty()) {
            configuration.setAllowedHeaders(allowedHeaders);
        } else {
            configuration.setAllowedHeaders(Arrays.asList(
                    "Authorization",
                    "Content-Type",
                    "X-Requested-With",
                    "Accept",
                    "Origin",
                    "Cache-Control",
                    "Content-Range",
                    "X-File-Name"
            ));
        }
        
        // Set exposed headers for client access
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Range",
                "X-Total-Count"
        ));
        
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(3600L); // 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
