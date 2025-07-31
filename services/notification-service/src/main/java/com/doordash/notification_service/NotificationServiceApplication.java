package com.doordash.notification_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

/**
 * Main application class for the Notification Service.
 * 
 * This service provides comprehensive notification management including:
 * - Email notifications via SendGrid/SMTP
 * - SMS notifications via Twilio
 * - Push notifications via Firebase FCM
 * - Real-time WebSocket notifications
 * - Template management and personalization
 * - Delivery tracking and analytics
 * - Rate limiting and retry mechanisms
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableKafka
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@EnableWebSocket
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
