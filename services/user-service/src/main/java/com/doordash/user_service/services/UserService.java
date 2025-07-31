package com.doordash.user_service.services;

import com.doordash.user_service.domain.dtos.user.*;
import com.doordash.user_service.domain.entities.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for user profile management.
 * 
 * Provides comprehensive user profile operations including CRUD operations,
 * search functionality, and integration with other services.
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
public interface UserService {

    /**
     * Create a new user profile.
     * 
     * @param request The profile creation request
     * @return Created user profile response
     */
    UserProfileResponse createProfile(CreateUserProfileRequest request);

    /**
     * Get user profile by user ID.
     * 
     * @param userId The user ID
     * @return User profile response or empty if not found
     */
    Optional<UserProfileResponse> getProfile(UUID userId);

    /**
     * Get user profile by ID.
     * 
     * @param id The profile ID
     * @return User profile response or empty if not found
     */
    Optional<UserProfileResponse> getProfileById(UUID id);

    /**
     * Update user profile.
     * 
     * @param userId The user ID
     * @param request The profile update request
     * @return Updated user profile response
     */
    UserProfileResponse updateProfile(UUID userId, UpdateUserProfileRequest request);

    /**
     * Partially update user profile.
     * 
     * @param userId The user ID
     * @param request The partial update request
     * @return Updated user profile response
     */
    UserProfileResponse patchProfile(UUID userId, PatchUserProfileRequest request);

    /**
     * Deactivate user profile (soft delete).
     * 
     * @param userId The user ID
     * @param performedBy Who performed the action
     */
    void deactivateProfile(UUID userId, String performedBy);

    /**
     * Reactivate user profile.
     * 
     * @param userId The user ID
     * @param performedBy Who performed the action
     * @return Reactivated user profile response
     */
    UserProfileResponse reactivateProfile(UUID userId, String performedBy);

    /**
     * Search user profiles by name.
     * 
     * @param searchTerm The search term
     * @param pageable Pagination parameters
     * @return Page of matching user profiles
     */
    Page<UserProfileResponse> searchProfiles(String searchTerm, Pageable pageable);

    /**
     * Get all active user profiles.
     * 
     * @param pageable Pagination parameters
     * @return Page of active user profiles
     */
    Page<UserProfileResponse> getAllActiveProfiles(Pageable pageable);

    /**
     * Get user profiles by verification status.
     * 
     * @param isVerified Verification status
     * @param pageable Pagination parameters
     * @return Page of user profiles
     */
    Page<UserProfileResponse> getProfilesByVerificationStatus(Boolean isVerified, Pageable pageable);

    /**
     * Get user profiles by verification level.
     * 
     * @param verificationLevel Verification level
     * @param pageable Pagination parameters
     * @return Page of user profiles
     */
    Page<UserProfileResponse> getProfilesByVerificationLevel(
            UserProfile.VerificationLevel verificationLevel, 
            Pageable pageable);

    /**
     * Update user verification status.
     * 
     * @param userId The user ID
     * @param isVerified New verification status
     * @param verificationLevel New verification level
     * @param performedBy Who performed the action
     * @return Updated user profile response
     */
    UserProfileResponse updateVerificationStatus(
            UUID userId, 
            Boolean isVerified, 
            UserProfile.VerificationLevel verificationLevel,
            String performedBy);

    /**
     * Upload user profile picture.
     * 
     * @param userId The user ID
     * @param request The file upload request
     * @return Updated user profile response with new picture URL
     */
    UserProfileResponse uploadProfilePicture(UUID userId, UploadProfilePictureRequest request);

    /**
     * Delete user profile picture.
     * 
     * @param userId The user ID
     * @return Updated user profile response
     */
    UserProfileResponse deleteProfilePicture(UUID userId);

    /**
     * Check if user profile exists.
     * 
     * @param userId The user ID
     * @return true if profile exists and is active
     */
    boolean profileExists(UUID userId);

    /**
     * Get user profiles by IDs (for batch operations).
     * 
     * @param userIds List of user IDs
     * @return List of user profile responses
     */
    List<UserProfileResponse> getProfilesByUserIds(List<UUID> userIds);

    /**
     * Get incomplete user profiles.
     * 
     * @param pageable Pagination parameters
     * @return Page of incomplete profiles
     */
    Page<UserProfileResponse> getIncompleteProfiles(Pageable pageable);

    /**
     * Get user profiles that need verification.
     * 
     * @param pageable Pagination parameters
     * @return Page of profiles needing verification
     */
    Page<UserProfileResponse> getProfilesNeedingVerification(Pageable pageable);

    /**
     * Get user profile statistics.
     * 
     * @return Profile statistics
     */
    UserProfileStatistics getProfileStatistics();

    /**
     * Validate user profile completeness.
     * 
     * @param userId The user ID
     * @return Profile validation result
     */
    ProfileValidationResult validateProfile(UUID userId);

    /**
     * Export user profile data (for GDPR compliance).
     * 
     * @param userId The user ID
     * @return Complete user data export
     */
    UserDataExportResponse exportUserData(UUID userId);
}
