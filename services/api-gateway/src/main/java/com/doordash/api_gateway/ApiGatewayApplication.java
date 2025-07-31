package com.doordash.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for the API Gateway Service.
 * 
 * This service serves as the single entry point for all microservices in the DoorDash platform.
 * It provides comprehensive routing, security, rate limiting, monitoring, and API versioning capabilities.
 * 
 * Key Features:
 * - Dynamic routing with load balancing
 * - JWT-based security and authentication
 * - Redis-based distributed rate limiting
 * - Request/response logging and audit trails
 * - API versioning support (v1, v2, etc.)
 * - Circuit breaker patterns for fault tolerance
 * - Global error handling and response standardization
 * - Prometheus metrics and distributed tracing
 * - CORS configuration and security headers
 * - Service discovery with Consul integration
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
