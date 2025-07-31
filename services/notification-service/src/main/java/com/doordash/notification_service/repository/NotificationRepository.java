package com.doordash.notification_service.repository;

import com.doordash.notification_service.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Notification entity operations.
 * Provides methods for querying notifications with various filters.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find notifications by user ID with pagination
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find notifications by user ID and type
     */
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, Notification.NotificationType type);

    /**
     * Find notifications by status
     */
    List<Notification> findByStatusOrderByCreatedAtAsc(Notification.NotificationStatus status);

    /**
     * Find notifications by status and type
     */
    List<Notification> findByStatusAndTypeOrderByCreatedAtAsc(
            Notification.NotificationStatus status, 
            Notification.NotificationType type);

    /**
     * Find pending notifications that can be retried
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND n.retryCount < n.maxRetries")
    List<Notification> findRetryableNotifications();

    /**
     * Find scheduled notifications that are ready to be sent
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.scheduledAt <= :now")
    List<Notification> findScheduledNotificationsReadyToSend(@Param("now") LocalDateTime now);

    /**
     * Find notifications by provider message ID
     */
    Optional<Notification> findByProviderMessageId(String providerMessageId);

    /**
     * Count notifications by user ID and status
     */
    long countByUserIdAndStatus(Long userId, Notification.NotificationStatus status);

    /**
     * Count notifications by status in date range
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status AND n.createdAt BETWEEN :start AND :end")
    long countByStatusInDateRange(
            @Param("status") Notification.NotificationStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Count notifications by type in date range
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.type = :type AND n.createdAt BETWEEN :start AND :end")
    long countByTypeInDateRange(
            @Param("type") Notification.NotificationType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Find failed notifications that exceeded max retries
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND n.retryCount >= n.maxRetries")
    List<Notification> findFailedNotificationsExceedingMaxRetries();

    /**
     * Find notifications by template ID
     */
    List<Notification> findByTemplateIdOrderByCreatedAtDesc(Long templateId);

    /**
     * Get notification statistics by user
     */
    @Query("SELECT n.status, COUNT(n) FROM Notification n WHERE n.userId = :userId GROUP BY n.status")
    List<Object[]> getNotificationStatsByUser(@Param("userId") Long userId);

    /**
     * Get notification statistics by type in date range
     */
    @Query("SELECT n.type, n.status, COUNT(n) FROM Notification n WHERE n.createdAt BETWEEN :start AND :end GROUP BY n.type, n.status")
    List<Object[]> getNotificationStatsByTypeInDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Delete old notifications (for cleanup)
     */
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate AND n.status IN ('DELIVERED', 'FAILED')")
    void deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find notifications with high priority
     */
    @Query("SELECT n FROM Notification n WHERE n.priority <= :priority AND n.status = 'PENDING' ORDER BY n.priority ASC, n.createdAt ASC")
    List<Notification> findHighPriorityPendingNotifications(@Param("priority") Integer priority);

    /**
     * Count unread in-app notifications for user
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.type = 'IN_APP' AND n.status != 'DELIVERED'")
    long countUnreadInAppNotifications(@Param("userId") Long userId);
}
