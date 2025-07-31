package com.doordash.auth_service.controllers;

import com.doordash.auth_service.domain.dtos.auth.*;
import com.doordash.auth_service.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 * 
 * This controller handles all authentication-related HTTP requests including
 * user registration, login, logout, token management, password operations,
 * and email verification. It provides comprehensive API documentation and
 * follows RESTful design principles.
 * 
 * Security Features:
 * - Rate limiting on sensitive endpoints
 * - Request validation and sanitization
 * - Comprehensive audit logging
 * - IP address and device tracking
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and authorization operations")
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user account.
     * 
     * @param request the registration request
     * @param httpRequest the HTTP request for extracting IP and user agent
     * @return authentication response with tokens
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account with email verification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "User already exists"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Registration attempt for email: {}", request.getEmail());
        
        // Enhance request with client information
        request.setDeviceInfo(extractDeviceInfo(httpRequest));
        
        AuthResponse response = authService.register(request);
        
        log.info("User registered successfully with ID: {}", response.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate user and generate tokens.
     * 
     * @param request the login request
     * @param httpRequest the HTTP request for extracting IP and user agent
     * @return authentication response with tokens
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with credentials and generate tokens")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "423", description = "Account locked"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Login attempt for user: {}", request.getUsernameOrEmail());
        
        // Enhance request with client information
        request.setIpAddress(getClientIpAddress(httpRequest));
        request.setUserAgent(httpRequest.getHeader("User-Agent"));
        request.setDeviceInfo(extractDeviceInfo(httpRequest));
        
        AuthResponse response = authService.login(request);
        
        log.info("User logged in successfully: {}", response.getUser().getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Logout user and revoke tokens.
     * 
     * @param request the logout request
     * @return success response
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and revoke refresh token")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        log.info("Logout request received");
        authService.logout(request);
        log.info("User logged out successfully");
        return ResponseEntity.ok().build();
    }

    /**
     * Refresh access token using refresh token.
     * 
     * @param request the refresh token request
     * @return new authentication response with fresh tokens
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Generate new access token using refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        AuthResponse response = authService.refreshToken(request);
        log.info("Token refreshed successfully for user: {}", response.getUser().getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Validate JWT access token.
     * 
     * @param token the JWT token to validate
     * @return token validation response
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate token", description = "Validate JWT access token and return user details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token is valid"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    public ResponseEntity<TokenValidationResponse> validateToken(
            @Parameter(description = "JWT token to validate")
            @RequestParam String token) {
        
        log.debug("Token validation request received");
        TokenValidationResponse response = authService.validateToken(token);
        return ResponseEntity.ok(response);
    }

    /**
     * Revoke a specific token.
     * 
     * @param request the token revocation request
     * @return success response
     */
    @PostMapping("/revoke")
    @Operation(summary = "Revoke token", description = "Revoke a specific refresh token")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token revoked successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> revokeToken(@Valid @RequestBody RevokeTokenRequest request) {
        log.info("Token revocation request received");
        authService.revokeToken(request);
        log.info("Token revoked successfully");
        return ResponseEntity.ok().build();
    }

    /**
     * Send email verification link.
     * 
     * @param request the email verification request
     * @return success response
     */
    @PostMapping("/verify-email")
    @Operation(summary = "Send email verification", description = "Send email verification link to user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verification email sent"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "409", description = "Email already verified")
    })
    public ResponseEntity<Void> sendEmailVerification(@Valid @RequestBody EmailVerificationRequest request) {
        log.info("Email verification request for: {}", request.getEmail());
        authService.sendEmailVerification(request);
        log.info("Email verification sent successfully");
        return ResponseEntity.ok().build();
    }

    /**
     * Verify email using verification token.
     * 
     * @param request the verification token request
     * @return success response
     */
    @PostMapping("/verify-email/confirm")
    @Operation(summary = "Verify email", description = "Verify user email using verification token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        log.info("Email verification confirmation received");
        authService.verifyEmail(request);
        log.info("Email verified successfully");
        return ResponseEntity.ok().build();
    }

    /**
     * Request password reset.
     * 
     * @param request the forgot password request
     * @return success response
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Send password reset link to user email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset email sent"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Password reset request for: {}", request.getEmail());
        authService.forgotPassword(request);
        log.info("Password reset email sent successfully");
        return ResponseEntity.ok().build();
    }

    /**
     * Reset password using reset token.
     * 
     * @param request the password reset request
     * @return success response
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset user password using reset token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid token or password"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired reset token")
    })
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Password reset confirmation received");
        authService.resetPassword(request);
        log.info("Password reset successfully");
        return ResponseEntity.ok().build();
    }

    /**
     * Change password for authenticated user.
     * 
     * @param request the password change request
     * @param userDetails the authenticated user details
     * @return success response
     */
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change password for authenticated user")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAuthority('user.write')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid old password or new password"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Password change request for user: {}", userDetails.getUsername());
        Long userId = extractUserIdFromPrincipal(userDetails);
        authService.changePassword(request, userId);
        log.info("Password changed successfully for user: {}", userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * Get user profile information.
     * 
     * @param userDetails the authenticated user details
     * @return user profile response
     */
    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get authenticated user profile information")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAuthority('user.read')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserProfileResponse> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserIdFromPrincipal(userDetails);
        UserProfileResponse response = authService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update user profile information.
     * 
     * @param request the profile update request
     * @param userDetails the authenticated user details
     * @return updated user profile response
     */
    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update authenticated user profile information")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAuthority('user.write')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid profile data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Profile update request for user: {}", userDetails.getUsername());
        Long userId = extractUserIdFromPrincipal(userDetails);
        UserProfileResponse response = authService.updateUserProfile(request, userId);
        log.info("Profile updated successfully for user: {}", userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Revoke all tokens for authenticated user (global logout).
     * 
     * @param userDetails the authenticated user details
     * @return success response
     */
    @PostMapping("/revoke-all")
    @Operation(summary = "Revoke all tokens", description = "Revoke all refresh tokens for authenticated user")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAuthority('user.write')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "All tokens revoked successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> revokeAllTokens(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Revoke all tokens request for user: {}", userDetails.getUsername());
        Long userId = extractUserIdFromPrincipal(userDetails);
        authService.revokeAllTokens(userId);
        log.info("All tokens revoked successfully for user: {}", userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * Extract client IP address from HTTP request.
     * 
     * @param request the HTTP request
     * @return client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * Extract device information from HTTP request.
     * 
     * @param request the HTTP request
     * @return device information string
     */
    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String acceptLanguage = request.getHeader("Accept-Language");
        return String.format("UA: %s, Lang: %s", 
            userAgent != null ? userAgent : "Unknown",
            acceptLanguage != null ? acceptLanguage : "Unknown");
    }

    /**
     * Extract user ID from authentication principal.
     * 
     * @param userDetails the user details from security context
     * @return user ID
     */
    private Long extractUserIdFromPrincipal(UserDetails userDetails) {
        // This implementation depends on your UserDetails implementation
        // You might need to cast to your custom UserDetails class
        // For now, returning a placeholder
        return 1L; // TODO: Implement based on your UserDetails implementation
    }
}
