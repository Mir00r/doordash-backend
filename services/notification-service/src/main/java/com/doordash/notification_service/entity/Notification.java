package com.doordash.notification_service.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Entity representing a notification in the system.
 * Supports multiple delivery channels including email, SMS, push, and WebSocket.
 */
@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "channel", nullable = false, length = 50)
    private String channel;

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Column(name = "subject", length = 500)
    private String subject;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "template_id")
    private Long templateId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context_data", columnDefinition = "jsonb")
    private Map<String, Object> contextData;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 5;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type")
    private ProviderType providerType;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    @Builder.Default
    private Integer maxRetries = 3;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<NotificationDeliveryLog> deliveryLogs;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<NotificationEvent> events;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum NotificationType {
        EMAIL, SMS, PUSH, IN_APP, WEBSOCKET
    }

    public enum NotificationStatus {
        PENDING, SENT, DELIVERED, FAILED, CANCELLED
    }

    public enum ProviderType {
        SENDGRID, SMTP, TWILIO, FCM, APNS, WEBSOCKET
    }

    /**
     * Check if the notification can be retried
     */
    public boolean canRetry() {
        return this.retryCount < this.maxRetries && 
               (this.status == NotificationStatus.FAILED || this.status == NotificationStatus.PENDING);
    }

    /**
     * Mark notification as failed with reason
     */
    public void markAsFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.failureReason = reason;
        this.failedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark notification as sent
     */
    public void markAsSent(String providerMessageId, ProviderType providerType) {
        this.status = NotificationStatus.SENT;
        this.providerMessageId = providerMessageId;
        this.providerType = providerType;
        this.sentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark notification as delivered
     */
    public void markAsDelivered() {
        this.status = NotificationStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Increment retry count
     */
    public void incrementRetryCount() {
        this.retryCount++;
        this.updatedAt = LocalDateTime.now();
    }
}
