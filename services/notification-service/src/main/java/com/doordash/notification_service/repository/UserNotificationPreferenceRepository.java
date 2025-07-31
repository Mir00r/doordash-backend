package com.doordash.notification_service.repository;

import com.doordash.notification_service.entity.UserNotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserNotificationPreference entity operations.
 * Provides methods for managing user notification preferences.
 */
@Repository
public interface UserNotificationPreferenceRepository extends JpaRepository<UserNotificationPreference, Long> {

    /**
     * Find preferences by user ID
     */
    Optional<UserNotificationPreference> findByUserId(Long userId);

    /**
     * Check if preferences exist for user
     */
    boolean existsByUserId(Long userId);

    /**
     * Find users with email notifications enabled
     */
    @Query("SELECT p.userId FROM UserNotificationPreference p WHERE p.emailEnabled = true")
    List<Long> findUserIdsWithEmailEnabled();

    /**
     * Find users with SMS notifications enabled
     */
    @Query("SELECT p.userId FROM UserNotificationPreference p WHERE p.smsEnabled = true")
    List<Long> findUserIdsWithSmsEnabled();

    /**
     * Find users with push notifications enabled
     */
    @Query("SELECT p.userId FROM UserNotificationPreference p WHERE p.pushEnabled = true")
    List<Long> findUserIdsWithPushEnabled();

    /**
     * Find users with marketing emails enabled
     */
    @Query("SELECT p.userId FROM UserNotificationPreference p WHERE p.marketingEmails = true")
    List<Long> findUserIdsWithMarketingEmailsEnabled();

    /**
     * Find users with promotional notifications enabled
     */
    @Query("SELECT p.userId FROM UserNotificationPreference p WHERE p.promotionalNotifications = true")
    List<Long> findUserIdsWithPromotionalNotificationsEnabled();

    /**
     * Find users with order updates enabled
     */
    @Query("SELECT p.userId FROM UserNotificationPreference p WHERE p.orderUpdates = true")
    List<Long> findUserIdsWithOrderUpdatesEnabled();

    /**
     * Find users with delivery updates enabled
     */
    @Query("SELECT p.userId FROM UserNotificationPreference p WHERE p.deliveryUpdates = true")
    List<Long> findUserIdsWithDeliveryUpdatesEnabled();

    /**
     * Find users by time zone
     */
    List<UserNotificationPreference> findByTimeZone(String timeZone);

    /**
     * Find users by preferred language
     */
    List<UserNotificationPreference> findByPreferredLanguage(String preferredLanguage);

    /**
     * Count users with specific notification type enabled
     */
    @Query("SELECT COUNT(p) FROM UserNotificationPreference p WHERE " +
           "(:notificationType = 'EMAIL' AND p.emailEnabled = true) OR " +
           "(:notificationType = 'SMS' AND p.smsEnabled = true) OR " +
           "(:notificationType = 'PUSH' AND p.pushEnabled = true) OR " +
           "(:notificationType = 'IN_APP' AND p.inAppEnabled = true)")
    long countUsersWithNotificationTypeEnabled(@Param("notificationType") String notificationType);

    /**
     * Find users who can receive marketing communications
     */
    @Query("SELECT p FROM UserNotificationPreference p WHERE p.marketingEmails = true OR p.promotionalNotifications = true")
    List<UserNotificationPreference> findUsersWhoCanReceiveMarketing();

    /**
     * Delete preferences for a user (when user is deleted)
     */
    void deleteByUserId(Long userId);

    /**
     * Update email preference for a user
     */
    @Query("UPDATE UserNotificationPreference p SET p.emailEnabled = :enabled WHERE p.userId = :userId")
    void updateEmailPreference(@Param("userId") Long userId, @Param("enabled") Boolean enabled);

    /**
     * Update SMS preference for a user
     */
    @Query("UPDATE UserNotificationPreference p SET p.smsEnabled = :enabled WHERE p.userId = :userId")
    void updateSmsPreference(@Param("userId") Long userId, @Param("enabled") Boolean enabled);

    /**
     * Update push preference for a user
     */
    @Query("UPDATE UserNotificationPreference p SET p.pushEnabled = :enabled WHERE p.userId = :userId")
    void updatePushPreference(@Param("userId") Long userId, @Param("enabled") Boolean enabled);

    /**
     * Update marketing emails preference for a user
     */
    @Query("UPDATE UserNotificationPreference p SET p.marketingEmails = :enabled WHERE p.userId = :userId")
    void updateMarketingEmailsPreference(@Param("userId") Long userId, @Param("enabled") Boolean enabled);
}
