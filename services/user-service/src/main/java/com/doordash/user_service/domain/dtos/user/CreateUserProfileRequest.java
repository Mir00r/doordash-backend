package com.doordash.user_service.domain.dtos.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating a new user profile.
 * 
 * This DTO contains all the necessary information to create a new user profile
 * with proper validation and documentation.
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new user profile")
public class CreateUserProfileRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "User ID from auth service", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    private UUID userId;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    @Schema(description = "User's first name", example = "John", required = true)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    @Schema(description = "User's last name", example = "Doe", required = true)
    private String lastName;

    @Size(max = 200, message = "Display name must be at most 200 characters")
    @Schema(description = "User's display name (optional, will be generated from first/last name if not provided)", example = "John D.")
    private String displayName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be in valid international format")
    @Schema(description = "User's phone number in international format", example = "+1234567890")
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    @Schema(description = "User's date of birth", example = "1990-01-15")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Size(max = 1000, message = "Bio must be at most 1000 characters")
    @Schema(description = "User's bio or description", example = "Food enthusiast from San Francisco")
    private String bio;

    @Schema(description = "Profile picture URL (will be validated if provided)", example = "https://example.com/profile.jpg")
    private String profilePictureUrl;

    /**
     * Validate the request data.
     * 
     * @return true if the request is valid
     */
    public boolean isValid() {
        return userId != null &&
               firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty() &&
               (phoneNumber == null || phoneNumber.matches("^\\+?[1-9]\\d{1,14}$")) &&
               (dateOfBirth == null || dateOfBirth.isBefore(LocalDate.now()));
    }

    /**
     * Get the full name from first and last name.
     * 
     * @return Full name
     */
    public String getFullName() {
        if (firstName == null || lastName == null) {
            return null;
        }
        return firstName.trim() + " " + lastName.trim();
    }

    /**
     * Get the display name or generate it from first and last name.
     * 
     * @return Display name
     */
    public String getEffectiveDisplayName() {
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName.trim();
        }
        return getFullName();
    }

    /**
     * Sanitize and normalize the input data.
     */
    public void sanitize() {
        if (firstName != null) {
            firstName = firstName.trim();
        }
        if (lastName != null) {
            lastName = lastName.trim();
        }
        if (displayName != null) {
            displayName = displayName.trim();
            if (displayName.isEmpty()) {
                displayName = null;
            }
        }
        if (phoneNumber != null) {
            phoneNumber = phoneNumber.trim();
            if (phoneNumber.isEmpty()) {
                phoneNumber = null;
            }
        }
        if (bio != null) {
            bio = bio.trim();
            if (bio.isEmpty()) {
                bio = null;
            }
        }
        if (profilePictureUrl != null) {
            profilePictureUrl = profilePictureUrl.trim();
            if (profilePictureUrl.isEmpty()) {
                profilePictureUrl = null;
            }
        }
    }
}
