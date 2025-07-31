package com.doordash.notification_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * WebSocket controller for real-time notification delivery.
 * Handles WebSocket connections and real-time message broadcasting.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle user subscription to notifications
     */
    @MessageMapping("/notifications/subscribe/{userId}")
    @SendTo("/topic/notifications/{userId}")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ADMIN')")
    public Map<String, Object> subscribeToNotifications(@DestinationVariable Long userId) {
        log.info("User {} subscribed to real-time notifications", userId);
        return Map.of(
                "type", "subscription_confirmed",
                "userId", userId,
                "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * Send real-time notification to user
     */
    public void sendNotificationToUser(Long userId, Map<String, Object> notification) {
        log.debug("Sending real-time notification to user: {}", userId);
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
    }

    /**
     * Send real-time notification to all connected users
     */
    public void broadcastNotification(Map<String, Object> notification) {
        log.debug("Broadcasting notification to all connected users");
        messagingTemplate.convertAndSend("/topic/notifications/broadcast", notification);
    }

    /**
     * Send typing indicator or status update
     */
    @MessageMapping("/notifications/status/{userId}")
    @SendTo("/topic/notifications/status/{userId}")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ADMIN')")
    public Map<String, Object> updateStatus(@DestinationVariable Long userId, Map<String, Object> status) {
        log.debug("Status update for user {}: {}", userId, status);
        return Map.of(
                "type", "status_update",
                "userId", userId,
                "status", status,
                "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * Handle heartbeat/ping messages
     */
    @MessageMapping("/notifications/ping/{userId}")
    @SendTo("/topic/notifications/pong/{userId}")
    @PreAuthorize("#userId == authentication.principal.userId")
    public Map<String, Object> handlePing(@DestinationVariable Long userId) {
        return Map.of(
                "type", "pong",
                "userId", userId,
                "timestamp", System.currentTimeMillis()
        );
    }
}
