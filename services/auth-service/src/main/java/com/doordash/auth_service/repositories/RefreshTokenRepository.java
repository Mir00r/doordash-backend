package com.doordash.auth_service.repositories;

import com.doordash.auth_service.domain.entities.RefreshToken;
import com.doordash.auth_service.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RefreshToken entity operations.
 * 
 * Handles data access for JWT refresh tokens including
 * token validation, cleanup of expired tokens, and
 * security-related operations.
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find a refresh token by its hash.
     * 
     * @param tokenHash the hashed token to search for
     * @return Optional containing the refresh token if found
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Find all active refresh tokens for a user.
     * 
     * @param user the user
     * @return List of active refresh tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revokedAt IS NULL AND rt.expiresAt > :now")
    List<RefreshToken> findActiveTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Find all refresh tokens for a user (including expired and revoked).
     * 
     * @param user the user
     * @return List of all refresh tokens for the user
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Find all refresh tokens for a user by user ID.
     * 
     * @param userId the user ID
     * @return List of all refresh tokens for the user
     */
    List<RefreshToken> findByUserId(Long userId);

    /**
     * Check if a specific token hash exists and is valid.
     * 
     * @param tokenHash the token hash to check
     * @param now the current timestamp
     * @return true if token exists and is valid, false otherwise
     */
    @Query("SELECT COUNT(rt) > 0 FROM RefreshToken rt WHERE rt.tokenHash = :tokenHash " +
           "AND rt.revokedAt IS NULL AND rt.expiresAt > :now")
    boolean existsByTokenHashAndNotRevokedAndNotExpired(@Param("tokenHash") String tokenHash, @Param("now") LocalDateTime now);

    /**
     * Revoke a refresh token by setting its revoked timestamp.
     * 
     * @param tokenHash the token hash to revoke
     * @param revokedAt the revocation timestamp
     * @return number of tokens updated
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :revokedAt WHERE rt.tokenHash = :tokenHash")
    int revokeByTokenHash(@Param("tokenHash") String tokenHash, @Param("revokedAt") LocalDateTime revokedAt);

    /**
     * Revoke all refresh tokens for a user.
     * 
     * @param user the user
     * @param revokedAt the revocation timestamp
     * @return number of tokens revoked
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :revokedAt WHERE rt.user = :user AND rt.revokedAt IS NULL")
    int revokeAllByUser(@Param("user") User user, @Param("revokedAt") LocalDateTime revokedAt);

    /**
     * Delete expired refresh tokens.
     * 
     * @param cutoffTime tokens that expired before this time will be deleted
     * @return number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :cutoffTime")
    int deleteExpiredTokens(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Delete revoked refresh tokens older than specified time.
     * 
     * @param cutoffTime revoked tokens older than this time will be deleted
     * @return number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revokedAt IS NOT NULL AND rt.revokedAt < :cutoffTime")
    int deleteRevokedTokensOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find refresh tokens that are about to expire.
     * 
     * @param expiryThreshold tokens expiring before this time
     * @return List of tokens about to expire
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < :expiryThreshold " +
           "AND rt.revokedAt IS NULL")
    List<RefreshToken> findTokensExpiringBefore(@Param("expiryThreshold") LocalDateTime expiryThreshold);

    /**
     * Count active refresh tokens for a user.
     * 
     * @param user the user
     * @param now the current timestamp
     * @return number of active tokens
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user " +
           "AND rt.revokedAt IS NULL AND rt.expiresAt > :now")
    long countActiveTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Find refresh tokens by device information.
     * 
     * @param user the user
     * @param deviceInfo the device information to match
     * @return List of tokens for the specified device
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.deviceInfo = :deviceInfo")
    List<RefreshToken> findByUserAndDeviceInfo(@Param("user") User user, @Param("deviceInfo") String deviceInfo);

    /**
     * Revoke refresh tokens by device information.
     * 
     * @param user the user
     * @param deviceInfo the device information
     * @param revokedAt the revocation timestamp
     * @return number of tokens revoked
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :revokedAt " +
           "WHERE rt.user = :user AND rt.deviceInfo = :deviceInfo AND rt.revokedAt IS NULL")
    int revokeByUserAndDeviceInfo(@Param("user") User user, @Param("deviceInfo") String deviceInfo, 
                                  @Param("revokedAt") LocalDateTime revokedAt);
}
