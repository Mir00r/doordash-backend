package com.doordash.auth_service.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.net.InetAddress;
import java.time.LocalDateTime;

/**
 * RefreshToken entity for managing JWT refresh tokens.
 * 
 * Refresh tokens are used to obtain new access tokens without requiring
 * the user to log in again. They have longer expiration times than access
 * tokens and can be revoked for security purposes.
 * 
 * Security features:
 * - Tokens are hashed before storage
 * - Device and IP tracking for security monitoring
 * - Ability to revoke tokens
 * - Automatic cleanup of expired tokens
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@Entity
@Table(name = "refresh_tokens")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    /**
     * Unique identifier for the refresh token.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user this refresh token belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Hashed version of the refresh token.
     * The actual token is never stored in plain text.
     */
    @Column(name = "token_hash", unique = true, nullable = false)
    private String tokenHash;

    /**
     * Timestamp when the token expires.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Timestamp when the token was revoked.
     * Null if the token is still active.
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /**
     * Information about the device that created this token.
     * Used for security monitoring and user session management.
     */
    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    /**
     * IP address from which the token was created.
     * Used for security monitoring.
     */
    @Column(name = "ip_address")
    private InetAddress ipAddress;

    /**
     * Timestamp when the token was created.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Check if the token is expired.
     * 
     * @return true if token is expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if the token has been revoked.
     * 
     * @return true if token has been revoked, false otherwise
     */
    public boolean isRevoked() {
        return revokedAt != null;
    }

    /**
     * Check if the token is valid (not expired and not revoked).
     * 
     * @return true if token is valid, false otherwise
     */
    public boolean isValid() {
        return !isExpired() && !isRevoked();
    }

    /**
     * Revoke the token.
     */
    public void revoke() {
        this.revokedAt = LocalDateTime.now();
    }

    /**
     * Get a masked version of the token hash for logging purposes.
     * Shows only the first and last 4 characters.
     * 
     * @return masked token hash
     */
    public String getMaskedTokenHash() {
        if (tokenHash == null || tokenHash.length() < 8) {
            return "****";
        }
        return tokenHash.substring(0, 4) + "****" + tokenHash.substring(tokenHash.length() - 4);
    }
}
