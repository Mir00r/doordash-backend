package com.doordash.delivery_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

/**
 * Main application class for the Delivery Service.
 * 
 * This service handles all delivery-related operations including driver management,
 * delivery tracking, route optimization, and real-time communication for the 
 * DoorDash platform.
 * 
 * Core Features:
 * - Driver registration and profile management
 * - Real-time delivery tracking and status updates
 * - Route optimization for efficient deliveries
 * - Driver earnings and payout calculations
 * - Geospatial operations for location-based services
 * - WebSocket-based real-time communication
 * - Integration with Google Maps for routing and geocoding
 * 
 * Advanced Capabilities:
 * - Machine learning-based ETA predictions
 * - Dynamic pricing and surge management
 * - Fleet management and analytics
 * - Driver performance monitoring
 * - Delivery zone management
 * - Emergency protocols and safety features
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableKafka
@EnableFeignClients
@EnableRetry
@EnableTransactionManagement
@EnableWebSocket
public class DeliveryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryServiceApplication.class, args);
    }
}
