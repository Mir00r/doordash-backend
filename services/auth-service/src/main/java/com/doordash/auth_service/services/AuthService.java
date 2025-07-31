package com.doordash.auth_service.services;

import com.doordash.auth_service.domain.entities.User;
import com.doordash.auth_service.domain.dtos.auth.*;

/**
 * Authentication service interface defining core authentication operations.
 * 
 * This service handles user authentication, token management, password operations,
 * and email verification. It provides a clean abstraction layer between the
 * presentation layer and the business logic.
 * 
 * Key Responsibilities:
 * - User registration and login
 * - JWT token generation and validation
 * - Password reset and change operations
 * - Email verification workflows
 * - Account security management
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
public interface AuthService {

    /**
     * Register a new user account.
     * 
     * @param request the registration request containing user details
     * @return authentication response with tokens and user info
     * @throws UserAlreadyExistsException if email or username already exists
     * @throws InvalidPasswordException if password doesn't meet requirements
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate a user and generate tokens.
     * 
     * @param request the login request containing credentials
     * @return authentication response with tokens and user info
     * @throws InvalidCredentialsException if credentials are invalid
     * @throws AccountLockedException if account is locked
     * @throws AccountNotVerifiedException if email is not verified
     */
    AuthResponse login(LoginRequest request);

    /**
     * Logout a user by revoking their tokens.
     * 
     * @param request the logout request containing token information
     */
    void logout(LogoutRequest request);

    /**
     * Refresh access token using refresh token.
     * 
     * @param request the refresh token request
     * @return new authentication response with fresh tokens
     * @throws InvalidTokenException if refresh token is invalid or expired
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Validate a JWT access token.
     * 
     * @param token the JWT token to validate
     * @return token validation response with user details
     * @throws InvalidTokenException if token is invalid or expired
     */
    TokenValidationResponse validateToken(String token);

    /**
     * Revoke a specific token.
     * 
     * @param request the token revocation request
     */
    void revokeToken(RevokeTokenRequest request);

    /**
     * Send email verification link to user.
     * 
     * @param request the email verification request
     * @throws UserNotFoundException if user is not found
     * @throws EmailAlreadyVerifiedException if email is already verified
     */
    void sendEmailVerification(EmailVerificationRequest request);

    /**
     * Verify user's email using verification token.
     * 
     * @param request the verification token request
     * @throws InvalidTokenException if token is invalid or expired
     */
    void verifyEmail(VerifyEmailRequest request);

    /**
     * Initiate password reset process by sending reset email.
     * 
     * @param request the forgot password request
     * @throws UserNotFoundException if user is not found
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Reset password using reset token.
     * 
     * @param request the password reset request
     * @throws InvalidTokenException if reset token is invalid or expired
     * @throws InvalidPasswordException if new password doesn't meet requirements
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * Change user's password (authenticated operation).
     * 
     * @param request the password change request
     * @param userId the ID of the authenticated user
     * @throws InvalidCredentialsException if old password is incorrect
     * @throws InvalidPasswordException if new password doesn't meet requirements
     */
    void changePassword(ChangePasswordRequest request, Long userId);

    /**
     * Get user profile information.
     * 
     * @param userId the user ID
     * @return user profile response
     * @throws UserNotFoundException if user is not found
     */
    UserProfileResponse getUserProfile(Long userId);

    /**
     * Update user profile information.
     * 
     * @param request the profile update request
     * @param userId the user ID
     * @return updated user profile response
     * @throws UserNotFoundException if user is not found
     * @throws ValidationException if profile data is invalid
     */
    UserProfileResponse updateUserProfile(UpdateProfileRequest request, Long userId);

    /**
     * Revoke all tokens for a user (global logout).
     * 
     * @param userId the user ID
     */
    void revokeAllTokens(Long userId);

    /**
     * Check if user account is locked.
     * 
     * @param userId the user ID
     * @return true if account is locked, false otherwise
     */
    boolean isAccountLocked(Long userId);

    /**
     * Unlock user account (admin operation).
     * 
     * @param userId the user ID to unlock
     * @param adminUserId the ID of the admin performing the operation
     */
    void unlockAccount(Long userId, Long adminUserId);
}
