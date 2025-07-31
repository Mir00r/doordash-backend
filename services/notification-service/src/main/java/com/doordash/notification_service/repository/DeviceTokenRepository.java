package com.doordash.notification_service.repository;

import com.doordash.notification_service.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DeviceToken entity operations.
 * Provides methods for managing device tokens for push notifications.
 */
@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    /**
     * Find all active device tokens for a user
     */
    List<DeviceToken> findByUserIdAndIsActiveOrderByLastUsedAtDesc(Long userId, Boolean isActive);

    /**
     * Find device token by user ID and device ID
     */
    Optional<DeviceToken> findByUserIdAndDeviceId(Long userId, String deviceId);

    /**
     * Find device token by token value
     */
    Optional<DeviceToken> findByToken(String token);

    /**
     * Find all device tokens for a user by platform
     */
    List<DeviceToken> findByUserIdAndPlatformAndIsActiveOrderByLastUsedAtDesc(
            Long userId, String platform, Boolean isActive);

    /**
     * Find all active device tokens by platform
     */
    List<DeviceToken> findByPlatformAndIsActiveOrderByLastUsedAtDesc(String platform, Boolean isActive);

    /**
     * Check if device token exists for user and device
     */
    boolean existsByUserIdAndDeviceId(Long userId, String deviceId);

    /**
     * Check if token exists
     */
    boolean existsByToken(String token);

    /**
     * Deactivate all tokens for a user
     */
    @Modifying
    @Query("UPDATE DeviceToken d SET d.isActive = false WHERE d.userId = :userId")
    void deactivateAllTokensForUser(@Param("userId") Long userId);

    /**
     * Deactivate specific device token
     */
    @Modifying
    @Query("UPDATE DeviceToken d SET d.isActive = false WHERE d.userId = :userId AND d.deviceId = :deviceId")
    void deactivateTokenForUserAndDevice(@Param("userId") Long userId, @Param("deviceId") String deviceId);

    /**
     * Update last used timestamp
     */
    @Modifying
    @Query("UPDATE DeviceToken d SET d.lastUsedAt = :lastUsedAt WHERE d.id = :id")
    void updateLastUsedAt(@Param("id") Long id, @Param("lastUsedAt") LocalDateTime lastUsedAt);

    /**
     * Delete inactive tokens older than specified date
     */
    @Modifying
    @Query("DELETE FROM DeviceToken d WHERE d.isActive = false AND d.updatedAt < :cutoffDate")
    void deleteInactiveTokensOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find tokens that haven't been used for a long time
     */
    @Query("SELECT d FROM DeviceToken d WHERE d.lastUsedAt < :cutoffDate AND d.isActive = true")
    List<DeviceToken> findStaleTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count active tokens for user
     */
    long countByUserIdAndIsActive(Long userId, Boolean isActive);

    /**
     * Count tokens by platform
     */
    long countByPlatformAndIsActive(String platform, Boolean isActive);

    /**
     * Find users with active device tokens
     */
    @Query("SELECT DISTINCT d.userId FROM DeviceToken d WHERE d.isActive = true")
    List<Long> findUserIdsWithActiveTokens();

    /**
     * Find users with active tokens for specific platform
     */
    @Query("SELECT DISTINCT d.userId FROM DeviceToken d WHERE d.platform = :platform AND d.isActive = true")
    List<Long> findUserIdsWithActiveTokensForPlatform(@Param("platform") String platform);

    /**
     * Update token value for existing device
     */
    @Modifying
    @Query("UPDATE DeviceToken d SET d.token = :token, d.updatedAt = CURRENT_TIMESTAMP WHERE d.userId = :userId AND d.deviceId = :deviceId")
    void updateTokenForUserAndDevice(
            @Param("userId") Long userId, 
            @Param("deviceId") String deviceId, 
            @Param("token") String token);

    /**
     * Delete all tokens for a user (when user is deleted)
     */
    void deleteByUserId(Long userId);
}
