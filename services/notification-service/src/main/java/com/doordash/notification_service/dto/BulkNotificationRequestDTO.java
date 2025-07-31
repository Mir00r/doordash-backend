package com.doordash.notification_service.dto;

import com.doordash.notification_service.entity.Notification;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for bulk notification requests.
 * Used when sending notifications to multiple recipients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkNotificationRequestDTO {

    private List<Long> userIds;
    private Notification.NotificationType type;
    private String channel;
    private String subject;
    private String content;
    private String templateName;
    private Map<String, Object> templateVariables;
    private Integer priority;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt;
    
    private Integer maxRetries;
    private String batchName;
    private String description;
}
