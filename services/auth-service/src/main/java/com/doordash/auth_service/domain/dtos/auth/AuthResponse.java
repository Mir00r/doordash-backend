package com.doordash.auth_service.domain.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for authentication response containing tokens and user information.
 * 
 * Returned after successful login or registration operations.
 * Contains all necessary information for client-side authentication state.
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    /**
     * JWT access token for API authentication.
     * Short-lived token used for authorizing API requests.
     */
    private String accessToken;

    /**
     * Refresh token for obtaining new access tokens.
     * Longer-lived token used to refresh expired access tokens.
     */
    private String refreshToken;

    /**
     * Type of the token (typically "Bearer").
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access token expiration time in seconds.
     */
    private Long expiresIn;

    /**
     * Refresh token expiration time.
     */
    private LocalDateTime refreshTokenExpiresAt;

    /**
     * User information included in the response.
     */
    private UserInfo user;

    /**
     * Nested class for user information in authentication response.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        
        /**
         * User's unique identifier.
         */
        private Long id;

        /**
         * User's email address.
         */
        private String email;

        /**
         * User's username.
         */
        private String username;

        /**
         * User's first name.
         */
        private String firstName;

        /**
         * User's last name.
         */
        private String lastName;

        /**
         * User's phone number.
         */
        private String phoneNumber;

        /**
         * Email verification status.
         */
        private Boolean isEmailVerified;

        /**
         * Phone verification status.
         */
        private Boolean isPhoneVerified;

        /**
         * Account active status.
         */
        private Boolean isActive;

        /**
         * Roles assigned to the user.
         */
        private Set<String> roles;

        /**
         * Permissions granted to the user (through roles).
         */
        private Set<String> permissions;

        /**
         * Timestamp of account creation.
         */
        private LocalDateTime createdAt;

        /**
         * Timestamp of last login.
         */
        private LocalDateTime lastLoginAt;

        /**
         * Get user's full name.
         * 
         * @return concatenated first and last name
         */
        public String getFullName() {
            return firstName + " " + lastName;
        }
    }

    /**
     * Session information for the authentication.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionInfo {
        
        /**
         * Session identifier.
         */
        private String sessionId;

        /**
         * Device information.
         */
        private String deviceInfo;

        /**
         * IP address of the session.
         */
        private String ipAddress;

        /**
         * Session creation time.
         */
        private LocalDateTime createdAt;

        /**
         * Session expiration time.
         */
        private LocalDateTime expiresAt;
    }
}
