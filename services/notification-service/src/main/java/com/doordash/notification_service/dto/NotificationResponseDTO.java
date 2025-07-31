package com.doordash.notification_service.dto;

import com.doordash.notification_service.entity.Notification;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for notification responses.
 * Used when returning notification data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponseDTO {

    private Long id;
    private Long userId;
    private Notification.NotificationType type;
    private String channel;
    private String recipient;
    private String subject;
    private String content;
    private Long templateId;
    private Map<String, Object> contextData;
    private Integer priority;
    private Notification.NotificationStatus status;
    private Notification.ProviderType providerType;
    private String providerMessageId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sentAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deliveredAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime failedAt;
    
    private String failureReason;
    private Integer retryCount;
    private Integer maxRetries;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Create from Notification entity
     */
    public static NotificationResponseDTO fromEntity(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .channel(notification.getChannel())
                .recipient(notification.getRecipient())
                .subject(notification.getSubject())
                .content(notification.getContent())
                .templateId(notification.getTemplateId())
                .contextData(notification.getContextData())
                .priority(notification.getPriority())
                .status(notification.getStatus())
                .providerType(notification.getProviderType())
                .providerMessageId(notification.getProviderMessageId())
                .scheduledAt(notification.getScheduledAt())
                .sentAt(notification.getSentAt())
                .deliveredAt(notification.getDeliveredAt())
                .failedAt(notification.getFailedAt())
                .failureReason(notification.getFailureReason())
                .retryCount(notification.getRetryCount())
                .maxRetries(notification.getMaxRetries())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
