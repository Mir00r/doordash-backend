package com.doordash.notification_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity for tracking notification events like opens, clicks, bounces, etc.
 * Used for analytics and engagement tracking.
 */
@Entity
@Table(name = "notification_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    @JsonBackReference
    private Notification notification;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data", columnDefinition = "jsonb")
    private Map<String, Object> eventData;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "ip_address")
    private InetAddress ipAddress;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Common event types
     */
    public static class EventType {
        public static final String SENT = "sent";
        public static final String DELIVERED = "delivered";
        public static final String OPENED = "opened";
        public static final String CLICKED = "clicked";
        public static final String BOUNCED = "bounced";
        public static final String FAILED = "failed";
        public static final String UNSUBSCRIBED = "unsubscribed";
        public static final String MARKED_AS_SPAM = "marked_as_spam";
    }

    /**
     * Create an event for notification sent
     */
    public static NotificationEvent createSentEvent(Notification notification, Map<String, Object> data) {
        return NotificationEvent.builder()
                .notification(notification)
                .eventType(EventType.SENT)
                .eventData(data)
                .build();
    }

    /**
     * Create an event for notification delivered
     */
    public static NotificationEvent createDeliveredEvent(Notification notification, Map<String, Object> data) {
        return NotificationEvent.builder()
                .notification(notification)
                .eventType(EventType.DELIVERED)
                .eventData(data)
                .build();
    }

    /**
     * Create an event for notification opened
     */
    public static NotificationEvent createOpenedEvent(
            Notification notification, 
            String userAgent, 
            InetAddress ipAddress, 
            Map<String, Object> data) {
        return NotificationEvent.builder()
                .notification(notification)
                .eventType(EventType.OPENED)
                .eventData(data)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();
    }

    /**
     * Create an event for notification clicked
     */
    public static NotificationEvent createClickedEvent(
            Notification notification, 
            String userAgent, 
            InetAddress ipAddress, 
            Map<String, Object> data) {
        return NotificationEvent.builder()
                .notification(notification)
                .eventType(EventType.CLICKED)
                .eventData(data)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();
    }

    /**
     * Create an event for notification bounced
     */
    public static NotificationEvent createBouncedEvent(Notification notification, Map<String, Object> data) {
        return NotificationEvent.builder()
                .notification(notification)
                .eventType(EventType.BOUNCED)
                .eventData(data)
                .build();
    }

    /**
     * Create an event for notification failed
     */
    public static NotificationEvent createFailedEvent(Notification notification, Map<String, Object> data) {
        return NotificationEvent.builder()
                .notification(notification)
                .eventType(EventType.FAILED)
                .eventData(data)
                .build();
    }
}
