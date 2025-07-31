package com.doordash.notification_service.repository;

import com.doordash.notification_service.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for NotificationTemplate entity operations.
 * Provides methods for managing notification templates.
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    /**
     * Find template by name
     */
    Optional<NotificationTemplate> findByName(String name);

    /**
     * Find template by name and active status
     */
    Optional<NotificationTemplate> findByNameAndIsActive(String name, Boolean isActive);

    /**
     * Find all active templates
     */
    List<NotificationTemplate> findByIsActiveOrderByNameAsc(Boolean isActive);

    /**
     * Find templates by type
     */
    List<NotificationTemplate> findByTypeOrderByNameAsc(NotificationTemplate.TemplateType type);

    /**
     * Find active templates by type
     */
    List<NotificationTemplate> findByTypeAndIsActiveOrderByNameAsc(
            NotificationTemplate.TemplateType type, 
            Boolean isActive);

    /**
     * Find latest version of a template by name
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.name = :name ORDER BY t.version DESC")
    Optional<NotificationTemplate> findLatestVersionByName(@Param("name") String name);

    /**
     * Find all versions of a template by name
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.name = :name ORDER BY t.version DESC")
    List<NotificationTemplate> findAllVersionsByName(@Param("name") String name);

    /**
     * Find templates created by user
     */
    List<NotificationTemplate> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    /**
     * Check if template name exists
     */
    boolean existsByName(String name);

    /**
     * Check if template name exists excluding current ID
     */
    @Query("SELECT COUNT(t) > 0 FROM NotificationTemplate t WHERE t.name = :name AND t.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    /**
     * Count templates by type
     */
    long countByType(NotificationTemplate.TemplateType type);

    /**
     * Count active templates
     */
    long countByIsActive(Boolean isActive);

    /**
     * Find templates with names like pattern
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.name LIKE %:pattern% AND t.isActive = true ORDER BY t.name ASC")
    List<NotificationTemplate> findByNameContainingAndActive(@Param("pattern") String pattern);

    /**
     * Deactivate old versions when a new version is created
     */
    @Query("UPDATE NotificationTemplate t SET t.isActive = false WHERE t.name = :name AND t.version < :version")
    void deactivateOldVersions(@Param("name") String name, @Param("version") Integer version);
}
