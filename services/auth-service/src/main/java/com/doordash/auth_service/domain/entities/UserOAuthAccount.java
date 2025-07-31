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

/**
 * UserOAuthAccount entity representing a user's linked OAuth account.
 * 
 * This entity stores the connection between a local user account
 * and an external OAuth provider account. It enables social login
 * and account linking functionality.
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@Entity
@Table(name = "user_oauth_accounts")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOAuthAccount {

    /**
     * Unique identifier for this OAuth account link.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The local user account.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The OAuth provider.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private OAuthProvider provider;

    /**
     * The user ID from the external OAuth provider.
     */
    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    /**
     * Access token from the OAuth provider.
     * Should be encrypted in production.
     */
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    /**
     * Refresh token from the OAuth provider.
     * Should be encrypted in production.
     */
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    /**
     * Timestamp when the OAuth tokens expire.
     */
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    /**
     * Timestamp when this OAuth account was linked.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when this OAuth account was last updated.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if the OAuth tokens are expired.
     * 
     * @return true if tokens are expired, false otherwise
     */
    public boolean areTokensExpired() {
        return tokenExpiresAt != null && LocalDateTime.now().isAfter(tokenExpiresAt);
    }

    /**
     * Update the OAuth tokens.
     * 
     * @param accessToken new access token
     * @param refreshToken new refresh token
     * @param expiresAt new expiration time
     */
    public void updateTokens(String accessToken, String refreshToken, LocalDateTime expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = expiresAt;
        this.updatedAt = LocalDateTime.now();
    }
}
