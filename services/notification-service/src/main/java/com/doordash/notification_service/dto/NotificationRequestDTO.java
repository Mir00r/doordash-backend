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
 * DTO for notification requests.
 * Used when creating new notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationRequestDTO {

    private Long userId;
    private Notification.NotificationType type;
    private String channel;
    private String recipient;
    private String subject;
    private String content;
    private String templateName;
    private Map<String, Object> templateVariables;
    private Integer priority;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt;
    
    private Integer maxRetries;

    /**
     * Convert to Notification entity
     */
    public Notification toEntity() {
        return Notification.builder()
                .userId(this.userId)
                .type(this.type)
                .channel(this.channel)
                .recipient(this.recipient)
                .subject(this.subject)
                .content(this.content)
                .contextData(this.templateVariables)
                .priority(this.priority != null ? this.priority : 5)
                .scheduledAt(this.scheduledAt)
                .maxRetries(this.maxRetries != null ? this.maxRetries : 3)
                .build();
    }
}
