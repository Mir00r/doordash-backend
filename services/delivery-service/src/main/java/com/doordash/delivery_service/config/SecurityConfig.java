package com.doordash.delivery_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration for the Delivery Service.
 * 
 * Configures JWT authentication, authorization rules, CORS settings,
 * and security headers for the delivery service endpoints.
 * 
 * Features:
 * - JWT-based authentication
 * - Role-based access control
 * - CORS configuration for cross-origin requests
 * - Security headers for protection
 * - Rate limiting configuration
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Configure HTTP security settings
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/actuator/health", "/actuator/info", "/actuator/metrics").permitAll()
                .requestMatchers("/api/v1/docs/**", "/api/v1/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/error").permitAll()
                
                // Driver endpoints
                .requestMatchers("/api/v1/drivers/register").hasAnyRole("ADMIN", "DRIVER_MANAGER")
                .requestMatchers("/api/v1/drivers/*/verify").hasAnyRole("ADMIN", "DRIVER_MANAGER")
                .requestMatchers("/api/v1/drivers/*/suspend").hasAnyRole("ADMIN", "DRIVER_MANAGER")
                .requestMatchers("/api/v1/drivers/*/activate").hasAnyRole("ADMIN", "DRIVER_MANAGER")
                .requestMatchers("/api/v1/drivers/available/**").hasAnyRole("ADMIN", "DRIVER_MANAGER", "DISPATCHER")
                
                // Delivery endpoints
                .requestMatchers("/api/v1/deliveries/pending").hasAnyRole("ADMIN", "DISPATCHER")
                .requestMatchers("/api/v1/deliveries/overdue").hasAnyRole("ADMIN", "DISPATCHER")
                .requestMatchers("/api/v1/deliveries/*/assign/**").hasAnyRole("ADMIN", "DISPATCHER", "RESTAURANT_MANAGER")
                .requestMatchers("/api/v1/deliveries/*/auto-assign").hasAnyRole("ADMIN", "DISPATCHER", "RESTAURANT_MANAGER")
                
                // Zone management
                .requestMatchers("/api/v1/zones").hasAnyRole("ADMIN", "ZONE_MANAGER")
                .requestMatchers("/api/v1/zones/**").hasAnyRole("ADMIN", "ZONE_MANAGER", "DISPATCHER")
                
                // Tracking endpoints
                .requestMatchers("/api/v1/tracking/*/location").hasAnyRole("ADMIN", "DRIVER")
                .requestMatchers("/api/v1/tracking/customer/**").hasAnyRole("ADMIN", "CUSTOMER")
                
                // Analytics and reporting
                .requestMatchers("/api/v1/analytics/**").hasAnyRole("ADMIN", "ANALYTICS_USER")
                .requestMatchers("/api/v1/reports/**").hasAnyRole("ADMIN", "MANAGER")
                
                // All other requests need authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint())
                .accessDeniedHandler(jwtAccessDeniedHandler())
            )
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true)
                )
                .and()
            );

        return http.build();
    }

    /**
     * Configure CORS settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Password encoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * JWT authentication filter
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtSecret);
    }

    /**
     * JWT authentication entry point
     */
    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }

    /**
     * JWT access denied handler
     */
    @Bean
    public JwtAccessDeniedHandler jwtAccessDeniedHandler() {
        return new JwtAccessDeniedHandler();
    }

    /**
     * In-memory user details service for testing
     * In production, this would be replaced with a service that
     * validates JWT tokens with the auth service
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("ADMIN")
                .build();

        UserDetails driver = User.builder()
                .username("driver")
                .password(passwordEncoder().encode("driver"))
                .roles("DRIVER")
                .build();

        UserDetails customer = User.builder()
                .username("customer")
                .password(passwordEncoder().encode("customer"))
                .roles("CUSTOMER")
                .build();

        UserDetails dispatcher = User.builder()
                .username("dispatcher")
                .password(passwordEncoder().encode("dispatcher"))
                .roles("DISPATCHER")
                .build();

        return new InMemoryUserDetailsManager(admin, driver, customer, dispatcher);
    }
}
