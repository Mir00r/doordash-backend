package com.doordash.user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for the User Service.
 * 
 * This service handles user profile management, address management, 
 * preferences, and user-related operations for the DoorDash platform.
 * 
 * Features:
 * - User profile management (CRUD operations)
 * - Address management with geocoding
 * - User preferences and settings
 * - Integration with Auth Service for user authentication
 * - Event-driven communication with other services
 * - File upload/download for profile pictures
 * - Analytics and activity tracking
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableKafka
@EnableFeignClients
@EnableRetry
@EnableTransactionManagement
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
