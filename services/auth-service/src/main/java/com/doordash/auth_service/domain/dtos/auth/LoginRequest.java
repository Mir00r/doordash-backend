package com.doordash.auth_service.domain.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user login requests.
 * 
 * Contains credentials and optional device information for authentication.
 * Supports login with either email or username.
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    /**
     * User's identifier (email or username).
     * Used for authentication along with password.
     */
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    /**
     * User's password.
     */
    @NotBlank(message = "Password is required")
    private String password;

    /**
     * Device information for security tracking and session management.
     * Optional but recommended for enhanced security.
     */
    private String deviceInfo;

    /**
     * Flag to remember the user's login session.
     * If true, refresh token will have extended expiration.
     */
    @Builder.Default
    private Boolean rememberMe = false;

    /**
     * Client IP address for security logging.
     * Usually populated by the controller from the request.
     */
    private String ipAddress;

    /**
     * User agent string from the client.
     * Used for device fingerprinting and security analysis.
     */
    private String userAgent;
}
