package com.doordash.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for the Auth Service.
 * 
 * This service handles authentication, authorization, and user management
 * for the DoorDash backend system. It provides JWT-based authentication,
 * OAuth2 integration, and comprehensive security features.
 * 
 * Key Features:
 * - User registration and login
 * - JWT token management
 * - Password reset and email verification
 * - Role-based access control
 * - Rate limiting and security policies
 * - Audit logging
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class AuthServiceApplication {

    /**
     * Main entry point for the Auth Service application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
