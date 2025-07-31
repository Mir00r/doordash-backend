package com.doordash.auth_service.domain.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user registration requests.
 * 
 * Contains all necessary information for creating a new user account
 * with comprehensive validation rules to ensure data integrity.
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    /**
     * User's email address.
     * Must be a valid email format and unique in the system.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * User's chosen username.
     * Must be unique and follow username format rules.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain letters, numbers, underscores, and hyphens")
    private String username;

    /**
     * User's password.
     * Must meet security requirements for strength.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    private String password;

    /**
     * Password confirmation for validation.
     * Must match the password field.
     */
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    /**
     * User's first name.
     */
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, apostrophes, and hyphens")
    private String firstName;

    /**
     * User's last name.
     */
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, apostrophes, and hyphens")
    private String lastName;

    /**
     * User's phone number (optional).
     */
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid international format")
    private String phoneNumber;

    /**
     * The role to assign to the user.
     * Defaults to CUSTOMER if not specified.
     */
    @Builder.Default
    private String role = "CUSTOMER";

    /**
     * Terms and conditions acceptance flag.
     * Must be true for registration to proceed.
     */
    @Builder.Default
    private Boolean acceptTerms = false;

    /**
     * Privacy policy acceptance flag.
     * Must be true for registration to proceed.
     */
    @Builder.Default
    private Boolean acceptPrivacyPolicy = false;

    /**
     * Marketing emails opt-in flag.
     * Optional, defaults to false.
     */
    @Builder.Default
    private Boolean allowMarketingEmails = false;

    /**
     * Device information for security tracking.
     */
    private String deviceInfo;

    /**
     * Check if passwords match.
     * 
     * @return true if passwords match, false otherwise
     */
    public boolean isPasswordsMatch() {
        return password != null && password.equals(confirmPassword);
    }

    /**
     * Check if required terms are accepted.
     * 
     * @return true if both terms and privacy policy are accepted
     */
    public boolean isTermsAccepted() {
        return Boolean.TRUE.equals(acceptTerms) && Boolean.TRUE.equals(acceptPrivacyPolicy);
    }

    /**
     * Get full name by combining first and last name.
     * 
     * @return full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
