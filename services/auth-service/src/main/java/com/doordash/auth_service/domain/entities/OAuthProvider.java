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
 * OAuthProvider entity representing external OAuth providers.
 * 
 * This entity stores configuration for external authentication providers
 * like Google, Facebook, Apple, etc. It enables social login functionality
 * and OAuth2 integration.
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@Entity
@Table(name = "oauth_providers")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthProvider {

    /**
     * Unique identifier for the OAuth provider.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the OAuth provider (e.g., google, facebook, apple).
     */
    @Column(unique = true, nullable = false, length = 50)
    private String name;

    /**
     * OAuth client ID provided by the external provider.
     */
    @Column(name = "client_id", nullable = false)
    private String clientId;

    /**
     * OAuth client secret provided by the external provider.
     * Should be encrypted in production.
     */
    @Column(name = "client_secret", nullable = false)
    private String clientSecret;

    /**
     * Authorization endpoint URL for the OAuth flow.
     */
    @Column(name = "authorization_uri", nullable = false, length = 500)
    private String authorizationUri;

    /**
     * Token endpoint URL for exchanging authorization codes for tokens.
     */
    @Column(name = "token_uri", nullable = false, length = 500)
    private String tokenUri;

    /**
     * User info endpoint URL for retrieving user information.
     */
    @Column(name = "user_info_uri", nullable = false, length = 500)
    private String userInfoUri;

    /**
     * OAuth scope requested from the provider.
     */
    @Column(length = 255)
    private String scope;

    /**
     * Flag indicating if this provider is currently active.
     */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Timestamp when the provider configuration was created.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the provider configuration was last updated.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * User OAuth accounts associated with this provider.
     */
    @Builder.Default
    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserOAuthAccount> userOAuthAccounts = new HashSet<>();
}
