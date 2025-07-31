package com.doordash.notification_service.repository;

import com.doordash.notification_service.entity.NotificationDeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for NotificationDeliveryLog entity operations.
 * Provides methods for tracking notification delivery statuses.
 */
@Repository
public interface NotificationDeliveryLogRepository extends JpaRepository<NotificationDeliveryLog, Long> {

    /**
     * Find delivery logs by notification ID
     */
    List<NotificationDeliveryLog> findByNotificationIdOrderByCreatedAtDesc(Long notificationId);

    /**
     * Find delivery log by provider message ID
     */
    Optional<NotificationDeliveryLog> findByProviderMessageId(String providerMessageId);

    /**
     * Find delivery logs by status
     */
    List<NotificationDeliveryLog> findByDeliveryStatusOrderByCreatedAtDesc(
            NotificationDeliveryLog.DeliveryStatus deliveryStatus);

    /**
     * Find delivery logs by provider type
     */
    List<NotificationDeliveryLog> findByProviderTypeOrderByCreatedAtDesc(
            com.doordash.notification_service.entity.Notification.ProviderType providerType);

    /**
     * Count delivery logs by status in date range
     */
    @Query("SELECT COUNT(dl) FROM NotificationDeliveryLog dl WHERE dl.deliveryStatus = :status AND dl.createdAt BETWEEN :start AND :end")
    long countByDeliveryStatusInDateRange(
            @Param("status") NotificationDeliveryLog.DeliveryStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Count delivery logs by provider type in date range
     */
    @Query("SELECT COUNT(dl) FROM NotificationDeliveryLog dl WHERE dl.providerType = :providerType AND dl.createdAt BETWEEN :start AND :end")
    long countByProviderTypeInDateRange(
            @Param("providerType") com.doordash.notification_service.entity.Notification.ProviderType providerType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Get delivery statistics by provider
     */
    @Query("SELECT dl.providerType, dl.deliveryStatus, COUNT(dl) FROM NotificationDeliveryLog dl GROUP BY dl.providerType, dl.deliveryStatus")
    List<Object[]> getDeliveryStatsByProvider();

    /**
     * Get delivery statistics by status in date range
     */
    @Query("SELECT dl.deliveryStatus, COUNT(dl) FROM NotificationDeliveryLog dl WHERE dl.createdAt BETWEEN :start AND :end GROUP BY dl.deliveryStatus")
    List<Object[]> getDeliveryStatsByStatusInDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Find bounced deliveries for cleanup
     */
    @Query("SELECT dl FROM NotificationDeliveryLog dl WHERE dl.deliveryStatus = 'BOUNCED' AND dl.bouncedAt IS NOT NULL")
    List<NotificationDeliveryLog> findBouncedDeliveries();

    /**
     * Find successful deliveries with engagement (opened/clicked)
     */
    @Query("SELECT dl FROM NotificationDeliveryLog dl WHERE dl.deliveryStatus IN ('OPENED', 'CLICKED')")
    List<NotificationDeliveryLog> findEngagedDeliveries();

    /**
     * Calculate delivery rate for provider in date range
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN dl.deliveryStatus = 'DELIVERED' THEN 1 END) * 100.0 / COUNT(dl) " +
           "FROM NotificationDeliveryLog dl " +
           "WHERE dl.providerType = :providerType AND dl.createdAt BETWEEN :start AND :end")
    Double calculateDeliveryRateForProvider(
            @Param("providerType") com.doordash.notification_service.entity.Notification.ProviderType providerType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Delete old delivery logs (for cleanup)
     */
    @Query("DELETE FROM NotificationDeliveryLog dl WHERE dl.createdAt < :cutoffDate")
    void deleteOldDeliveryLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
}
