package com.doordash.user_service.repositories;

import com.doordash.user_service.domain.entities.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserProfile entity.
 * 
 * Provides comprehensive data access methods for user profile management
 * including search, filtering, and analytics queries.
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    /**
     * Find user profile by user ID.
     * 
     * @param userId The user ID from auth service
     * @return Optional UserProfile
     */
    Optional<UserProfile> findByUserId(UUID userId);

    /**
     * Find active user profile by user ID.
     * 
     * @param userId The user ID
     * @return Optional UserProfile that is active
     */
    Optional<UserProfile> findByUserIdAndIsActiveTrue(UUID userId);

    /**
     * Check if user profile exists by user ID.
     * 
     * @param userId The user ID
     * @return true if profile exists
     */
    boolean existsByUserId(UUID userId);

    /**
     * Check if user profile exists and is active.
     * 
     * @param userId The user ID
     * @return true if active profile exists
     */
    boolean existsByUserIdAndIsActiveTrue(UUID userId);

    /**
     * Find all active user profiles.
     * 
     * @param pageable Pagination parameters
     * @return Page of active user profiles
     */
    Page<UserProfile> findByIsActiveTrue(Pageable pageable);

    /**
     * Find user profiles by verification status.
     * 
     * @param isVerified Verification status
     * @param pageable Pagination parameters
     * @return Page of user profiles
     */
    Page<UserProfile> findByIsVerifiedAndIsActiveTrue(Boolean isVerified, Pageable pageable);

    /**
     * Find user profiles by verification level.
     * 
     * @param verificationLevel Verification level
     * @param pageable Pagination parameters
     * @return Page of user profiles
     */
    Page<UserProfile> findByVerificationLevelAndIsActiveTrue(
            UserProfile.VerificationLevel verificationLevel, 
            Pageable pageable);

    /**
     * Search user profiles by name (first name, last name, or display name).
     * 
     * @param searchTerm The search term
     * @param pageable Pagination parameters
     * @return Page of matching user profiles
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true AND " +
           "(LOWER(up.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(up.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(up.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<UserProfile> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find user profiles by phone number.
     * 
     * @param phoneNumber The phone number
     * @return Optional UserProfile
     */
    Optional<UserProfile> findByPhoneNumberAndIsActiveTrue(String phoneNumber);

    /**
     * Find user profiles created within a date range.
     * 
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination parameters
     * @return Page of user profiles
     */
    Page<UserProfile> findByCreatedAtBetweenAndIsActiveTrue(
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            Pageable pageable);

    /**
     * Find recently updated user profiles.
     * 
     * @param since Date to check from
     * @param pageable Pagination parameters
     * @return Page of recently updated profiles
     */
    Page<UserProfile> findByUpdatedAtAfterAndIsActiveTrue(
            LocalDateTime since, 
            Pageable pageable);

    /**
     * Count total active user profiles.
     * 
     * @return Total count of active profiles
     */
    long countByIsActiveTrue();

    /**
     * Count verified user profiles.
     * 
     * @return Count of verified profiles
     */
    long countByIsVerifiedTrueAndIsActiveTrue();

    /**
     * Count user profiles by verification level.
     * 
     * @param verificationLevel Verification level
     * @return Count of profiles with the specified verification level
     */
    long countByVerificationLevelAndIsActiveTrue(UserProfile.VerificationLevel verificationLevel);

    /**
     * Find user profiles with incomplete information.
     * 
     * @param pageable Pagination parameters
     * @return Page of profiles with missing information
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true AND " +
           "(up.firstName IS NULL OR up.firstName = '' OR " +
           "up.lastName IS NULL OR up.lastName = '' OR " +
           "up.phoneNumber IS NULL OR up.phoneNumber = '')")
    Page<UserProfile> findIncompleteProfiles(Pageable pageable);

    /**
     * Find user profiles that need verification.
     * 
     * @param pageable Pagination parameters
     * @return Page of unverified profiles
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true AND " +
           "up.isVerified = false AND up.phoneNumber IS NOT NULL")
    Page<UserProfile> findProfilesNeedingVerification(Pageable pageable);

    /**
     * Get user profile statistics.
     * 
     * @return List of statistics
     */
    @Query("SELECT " +
           "COUNT(*) as totalProfiles, " +
           "COUNT(CASE WHEN up.isVerified = true THEN 1 END) as verifiedProfiles, " +
           "COUNT(CASE WHEN up.verificationLevel = 'FULL' THEN 1 END) as fullyVerifiedProfiles, " +
           "COUNT(CASE WHEN up.profilePictureUrl IS NOT NULL THEN 1 END) as profilesWithPictures " +
           "FROM UserProfile up WHERE up.isActive = true")
    Object[] getProfileStatistics();

    /**
     * Find users by user IDs (for batch operations).
     * 
     * @param userIds List of user IDs
     * @return List of user profiles
     */
    List<UserProfile> findByUserIdInAndIsActiveTrue(List<UUID> userIds);

    /**
     * Soft delete user profile by user ID.
     * 
     * @param userId The user ID
     * @param updatedBy Who performed the update
     * @return Number of affected rows
     */
    @Query("UPDATE UserProfile up SET up.isActive = false, up.updatedAt = CURRENT_TIMESTAMP, " +
           "up.updatedBy = :updatedBy WHERE up.userId = :userId")
    int softDeleteByUserId(@Param("userId") UUID userId, @Param("updatedBy") String updatedBy);
}
