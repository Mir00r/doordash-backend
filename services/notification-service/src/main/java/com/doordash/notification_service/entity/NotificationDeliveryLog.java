package com.doordash.notification_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity for tracking notification delivery logs and provider responses.
 * Stores detailed information about delivery attempts and their outcomes.
 */
@Entity
@Table(name = "notification_delivery_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    @JsonBackReference
    private Notification notification;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false)
    private Notification.ProviderType providerType;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    private DeliveryStatus deliveryStatus;

    @Column(name = "response_code", length = 50)
    private String responseCode;

    @Column(name = "response_message", columnDefinition = "TEXT")
    private String responseMessage;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    @Column(name = "bounced_at")
    private LocalDateTime bouncedAt;

    @Column(name = "unsubscribed_at")
    private LocalDateTime unsubscribedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum DeliveryStatus {
        PENDING, SENT, DELIVERED, FAILED, BOUNCED, CLICKED, OPENED
    }

    /**
     * Mark as delivered
     */
    public void markAsDelivered() {
        this.deliveryStatus = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    /**
     * Mark as opened (for email notifications)
     */
    public void markAsOpened() {
        this.deliveryStatus = DeliveryStatus.OPENED;
        this.openedAt = LocalDateTime.now();
    }

    /**
     * Mark as clicked (for email/push notifications)
     */
    public void markAsClicked() {
        this.deliveryStatus = DeliveryStatus.CLICKED;
        this.clickedAt = LocalDateTime.now();
    }

    /**
     * Mark as bounced
     */
    public void markAsBounced() {
        this.deliveryStatus = DeliveryStatus.BOUNCED;
        this.bouncedAt = LocalDateTime.now();
    }

    /**
     * Mark as failed with response details
     */
    public void markAsFailed(String responseCode, String responseMessage) {
        this.deliveryStatus = DeliveryStatus.FAILED;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }

    /**
     * Create a delivery log for a sent notification
     */
    public static NotificationDeliveryLog createSentLog(
            Notification notification, 
            Notification.ProviderType providerType, 
            String providerMessageId) {
        return NotificationDeliveryLog.builder()
                .notification(notification)
                .providerType(providerType)
                .providerMessageId(providerMessageId)
                .deliveryStatus(DeliveryStatus.SENT)
                .build();
    }

    /**
     * Create a delivery log for a failed notification
     */
    public static NotificationDeliveryLog createFailedLog(
            Notification notification, 
            Notification.ProviderType providerType, 
            String responseCode, 
            String responseMessage) {
        return NotificationDeliveryLog.builder()
                .notification(notification)
                .providerType(providerType)
                .deliveryStatus(DeliveryStatus.FAILED)
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }
}
