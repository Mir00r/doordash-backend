package com.doordash.auth_service.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * PasswordResetToken entity for handling password reset process.
 * 
 * When users request a password reset, a secure token is generated
 * and sent to their verified email address. This entity tracks those
 * tokens and their usage status.
 * 
 * Security considerations:
 * - Tokens should be cryptographically secure random values
 * - Tokens have short expiration time (typically 1 hour)
 * - Used tokens are tracked to prevent replay attacks
 * - Old tokens should be invalidated when new ones are created
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@Entity
@Table(name = "password_reset_tokens")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    /**
     * Unique identifier for the password reset token.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user this password reset token belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The password reset token string sent to the user's email.
     * Should be a cryptographically secure random value.
     */
    @Column(unique = true, nullable = false)
    private String token;

    /**
     * Timestamp when the token expires.
     * Expired tokens cannot be used for password reset.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Timestamp when the token was used for password reset.
     * Null if the token hasn't been used yet.
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

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
     * Check if the token has been used.
     * 
     * @return true if token has been used, false otherwise
     */
    public boolean isUsed() {
        return usedAt != null;
    }

    /**
     * Check if the token is valid (not expired and not used).
     * 
     * @return true if token is valid, false otherwise
     */
    public boolean isValid() {
        return !isExpired() && !isUsed();
    }

    /**
     * Mark the token as used.
     */
    public void markAsUsed() {
        this.usedAt = LocalDateTime.now();
    }
}
