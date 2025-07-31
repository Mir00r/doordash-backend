package com.doordash.notification_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time notifications.
 * Configures STOMP messaging and WebSocket endpoints.
 */
@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${notification.websocket.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${notification.websocket.heartbeat-interval:30000}")
    private long heartbeatInterval;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to carry messages back to the client
        config.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{heartbeatInterval, heartbeatInterval});
        
        // Prefix for messages bound to @MessageMapping-annotated methods
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
        
        log.info("WebSocket message broker configured with heartbeat interval: {}ms", heartbeatInterval);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the WebSocket endpoint that clients will use to connect
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins)
                .withSockJS()
                .setHeartbeatTime(heartbeatInterval);
        
        // Also register endpoint without SockJS for native WebSocket clients
        registry.addEndpoint("/ws/native")
                .setAllowedOrigins(allowedOrigins);
        
        log.info("WebSocket endpoints registered with allowed origins: {}", (Object) allowedOrigins);
    }
}
