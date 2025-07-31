package com.doordash.notification_service.entity;

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
 * Entity representing notification templates for different types of notifications.
 * Templates support variable substitution and versioning.
 */
@Entity
@Table(name = "notification_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TemplateType type;

    @Column(name = "subject_template", length = 500)
    private String subjectTemplate;

    @Column(name = "content_template", nullable = false, columnDefinition = "TEXT")
    private String contentTemplate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "variables", columnDefinition = "jsonb")
    private Map<String, Object> variables;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum TemplateType {
        EMAIL, SMS, PUSH, IN_APP
    }

    /**
     * Check if template is valid for use
     */
    public boolean isValid() {
        return this.isActive && 
               this.contentTemplate != null && 
               !this.contentTemplate.trim().isEmpty();
    }

    /**
     * Create a new version of this template
     */
    public NotificationTemplate createNewVersion() {
        return NotificationTemplate.builder()
                .name(this.name)
                .type(this.type)
                .subjectTemplate(this.subjectTemplate)
                .contentTemplate(this.contentTemplate)
                .variables(this.variables)
                .isActive(true)
                .version(this.version + 1)
                .createdBy(this.createdBy)
                .build();
    }

    /**
     * Deactivate this template
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
}
