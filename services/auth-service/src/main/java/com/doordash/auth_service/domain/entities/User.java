package com.doordash.auth_service.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing a user in the authentication system.
 * 
 * This entity stores user account information including credentials,
 * personal information, and account status. It supports various user types
 * such as customers, restaurant owners, delivery drivers, and administrators.
 * 
 * Security Features:
 * - Password hashing with BCrypt
 * - Account lockout mechanism
 * - Email and phone verification
 * - Failed login attempt tracking
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User's email address. Must be unique across the system.
     * Used as the primary login identifier.
     */
    @Column(unique = true, nullable = false, length = 255)
    private String email;

    /**
     * User's username. Must be unique across the system.
     * Alternative login identifier.
     */
    @Column(unique = true, nullable = false, length = 100)
    private String username;

    /**
     * BCrypt hashed password. Never store plain text passwords.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * User's first name.
     */
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    /**
     * User's last name.
     */
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /**
     * User's phone number for SMS notifications and verification.
     */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /**
     * Flag indicating if the user's email has been verified.
     */
    @Builder.Default
    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;

    /**
     * Flag indicating if the user's phone number has been verified.
     */
    @Builder.Default
    @Column(name = "is_phone_verified", nullable = false)
    private Boolean isPhoneVerified = false;

    /**
     * Flag indicating if the user account is active.
     * Inactive accounts cannot log in.
     */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Number of consecutive failed login attempts.
     * Used for account lockout mechanism.
     */
    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    /**
     * Timestamp until which the account is locked.
     * Null if account is not locked.
     */
    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    /**
     * Timestamp of the user's last successful login.
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * Timestamp when the user account was created.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user account was last updated.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Roles assigned to this user.
     * Many-to-many relationship with Role entity.
     */
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserRole> userRoles = new HashSet<>();

    /**
     * Email verification tokens associated with this user.
     */
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<EmailVerificationToken> emailVerificationTokens = new HashSet<>();

    /**
     * Password reset tokens associated with this user.
     */
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PasswordResetToken> passwordResetTokens = new HashSet<>();

    /**
     * Refresh tokens associated with this user.
     */
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    /**
     * Active sessions for this user.
     */
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserSession> userSessions = new HashSet<>();

    /**
     * OAuth accounts linked to this user.
     */
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserOAuthAccount> oauthAccounts = new HashSet<>();

    /**
     * Check if the user account is currently locked.
     * 
     * @return true if account is locked, false otherwise
     */
    public boolean isAccountLocked() {
        return accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * Check if the user account is enabled (active and not locked).
     * 
     * @return true if account is enabled, false otherwise
     */
    public boolean isAccountEnabled() {
        return isActive && !isAccountLocked();
    }

    /**
     * Get the user's full name.
     * 
     * @return concatenated first and last name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Reset failed login attempts counter.
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }

    /**
     * Increment failed login attempts and lock account if threshold is reached.
     * 
     * @param maxAttempts maximum allowed failed attempts
     * @param lockoutDurationMinutes lockout duration in minutes
     */
    public void incrementFailedLoginAttempts(int maxAttempts, long lockoutDurationMinutes) {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= maxAttempts) {
            this.accountLockedUntil = LocalDateTime.now().plusMinutes(lockoutDurationMinutes);
        }
    }

    /**
     * Update last login timestamp.
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
