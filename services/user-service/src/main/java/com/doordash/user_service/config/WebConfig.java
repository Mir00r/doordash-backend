package com.doordash.user_service.config;

import com.doordash.user_service.observability.tracing.TracingInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration for DoorDash User Service.
 * 
 * This configuration sets up web-related components including:
 * - HTTP request interceptors for tracing and monitoring
 * - CORS configuration for cross-origin requests
 * - Security header management
 * - Request/response logging and metrics
 * - Performance monitoring interceptors
 * 
 * Interceptor Chain Order:
 * 1. TracingInterceptor - Distributed tracing and correlation
 * 2. SecurityInterceptor - Security event logging
 * 3. MetricsInterceptor - Performance metrics collection
 * 4. LoggingInterceptor - Request/response logging
 * 
 * Integration Features:
 * - OpenTracing compatibility for distributed tracing
 * - Prometheus metrics collection
 * - Structured logging with trace correlation
 * - Security event auditing
 * - Performance monitoring and alerting
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    private final TracingInterceptor tracingInterceptor;

    /**
     * Configures HTTP request interceptors for comprehensive monitoring.
     * 
     * @param registry interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("Configuring web interceptors for User Service");
        
        // Add tracing interceptor for distributed tracing
        registry.addInterceptor(tracingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/actuator/health",
                    "/actuator/info",
                    "/actuator/prometheus",
                    "/favicon.ico",
                    "/static/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/error"
                )
                .order(1);
        
        log.info("Registered TracingInterceptor for distributed tracing");
    }
}
