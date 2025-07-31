package com.doordash.notification_service.service;

import com.doordash.notification_service.entity.NotificationTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for notification template management.
 * Provides methods for managing notification templates and processing template variables.
 */
public interface TemplateService {

    /**
     * Create a new notification template
     */
    NotificationTemplate createTemplate(NotificationTemplate template);

    /**
     * Update an existing template
     */
    NotificationTemplate updateTemplate(Long id, NotificationTemplate template);

    /**
     * Get template by ID
     */
    Optional<NotificationTemplate> getTemplateById(Long id);

    /**
     * Get template by name
     */
    Optional<NotificationTemplate> getTemplateByName(String name);

    /**
     * Get latest version of template by name
     */
    Optional<NotificationTemplate> getLatestTemplateByName(String name);

    /**
     * Get all active templates
     */
    List<NotificationTemplate> getAllActiveTemplates();

    /**
     * Get templates by type
     */
    List<NotificationTemplate> getTemplatesByType(NotificationTemplate.TemplateType type);

    /**
     * Get all versions of a template
     */
    List<NotificationTemplate> getAllVersionsByName(String name);

    /**
     * Create a new version of existing template
     */
    NotificationTemplate createNewVersion(String templateName, NotificationTemplate newTemplate);

    /**
     * Deactivate a template
     */
    void deactivateTemplate(Long id);

    /**
     * Activate a template
     */
    void activateTemplate(Long id);

    /**
     * Delete a template
     */
    void deleteTemplate(Long id);

    /**
     * Process template with variables to generate content
     */
    String processTemplate(String templateContent, Map<String, Object> variables);

    /**
     * Process subject template with variables
     */
    String processSubjectTemplate(String subjectTemplate, Map<String, Object> variables);

    /**
     * Validate template syntax
     */
    boolean validateTemplate(String templateContent);

    /**
     * Get template variables from content
     */
    List<String> extractVariablesFromTemplate(String templateContent);

    /**
     * Check if template name is available
     */
    boolean isTemplateNameAvailable(String name);

    /**
     * Search templates by name pattern
     */
    List<NotificationTemplate> searchTemplatesByName(String pattern);

    /**
     * Get template usage statistics
     */
    Map<String, Long> getTemplateUsageStats();

    /**
     * Clone template with new name
     */
    NotificationTemplate cloneTemplate(Long templateId, String newName);
}
