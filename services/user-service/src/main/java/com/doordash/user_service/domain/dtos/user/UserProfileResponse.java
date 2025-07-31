package com.doordash.user_service.domain.dtos.user;

import com.doordash.user_service.domain.entities.UserProfile;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for user profile information.
 * 
 * This DTO represents user profile data returned from API endpoints,
 * excluding sensitive information and internal fields.
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User profile information response")
public class UserProfileResponse {

    @Schema(description = "Profile unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "User ID from auth service", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @Schema(description = "User's display name", example = "John D.")
    private String displayName;

    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @Schema(description = "User's phone number", example = "+1234567890")
    private String phoneNumber;

    @Schema(description = "User's date of birth", example = "1990-01-15")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Schema(description = "Profile picture URL", example = "https://cdn.doordash.com/profiles/123.jpg")
    private String profilePictureUrl;

    @Schema(description = "User's bio or description", example = "Food enthusiast from San Francisco")
    private String bio;

    @Schema(description = "Whether the profile is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Whether the user is verified", example = "true")
    private Boolean isVerified;

    @Schema(description = "User's verification level", example = "FULL")
    private UserProfile.VerificationLevel verificationLevel;

    @Schema(description = "Whether the profile is complete", example = "true")
    private Boolean isComplete;

    @Schema(description = "Whether the user is eligible for advanced features", example = "true")
    private Boolean isEligibleForAdvancedFeatures;

    @Schema(description = "Profile creation timestamp", example = "2023-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Profile last update timestamp", example = "2023-06-15T14:45:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Create UserProfileResponse from UserProfile entity.
     * 
     * @param userProfile The user profile entity
     * @return UserProfileResponse DTO
     */
    public static UserProfileResponse fromEntity(UserProfile userProfile) {
        if (userProfile == null) {
            return null;
        }

        return UserProfileResponse.builder()
                .id(userProfile.getId())
                .userId(userProfile.getUserId())
                .firstName(userProfile.getFirstName())
                .lastName(userProfile.getLastName())
                .displayName(userProfile.getDisplayName())
                .fullName(userProfile.getFullName())
                .phoneNumber(userProfile.getPhoneNumber())
                .dateOfBirth(userProfile.getDateOfBirth())
                .profilePictureUrl(userProfile.getProfilePictureUrl())
                .bio(userProfile.getBio())
                .isActive(userProfile.getIsActive())
                .isVerified(userProfile.getIsVerified())
                .verificationLevel(userProfile.getVerificationLevel())
                .isComplete(userProfile.isProfileComplete())
                .isEligibleForAdvancedFeatures(userProfile.isEligibleForAdvancedFeatures())
                .createdAt(userProfile.getCreatedAt())
                .updatedAt(userProfile.getUpdatedAt())
                .build();
    }

    /**
     * Create a minimal UserProfileResponse with only essential fields.
     * 
     * @param userProfile The user profile entity
     * @return Minimal UserProfileResponse DTO
     */
    public static UserProfileResponse toMinimal(UserProfile userProfile) {
        if (userProfile == null) {
            return null;
        }

        return UserProfileResponse.builder()
                .id(userProfile.getId())
                .userId(userProfile.getUserId())
                .displayName(userProfile.getDisplayName())
                .fullName(userProfile.getFullName())
                .profilePictureUrl(userProfile.getProfilePictureUrl())
                .isVerified(userProfile.getIsVerified())
                .verificationLevel(userProfile.getVerificationLevel())
                .build();
    }

    /**
     * Create a public UserProfileResponse with only publicly visible fields.
     * 
     * @param userProfile The user profile entity
     * @return Public UserProfileResponse DTO
     */
    public static UserProfileResponse toPublic(UserProfile userProfile) {
        if (userProfile == null) {
            return null;
        }

        return UserProfileResponse.builder()
                .userId(userProfile.getUserId())
                .displayName(userProfile.getDisplayName())
                .profilePictureUrl(userProfile.getProfilePictureUrl())
                .isVerified(userProfile.getIsVerified())
                .build();
    }
}
