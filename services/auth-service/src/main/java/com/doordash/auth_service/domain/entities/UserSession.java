package com.doordash.auth_service.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.net.InetAddress;
import java.time.LocalDateTime;

/**
 * UserSession entity for tracking active user sessions.
 * 
 * This entity maintains information about active user sessions,
 * including device information, IP addresses, and session expiration.
 * It enables features like session management, security monitoring,
 * and concurrent session limiting.
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@Entity
@Table(name = "user_sessions")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    /**
     * Unique identifier for the session.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user this session belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Unique session identifier.
     */
    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;

    /**
     * Information about the device used for this session.
     */
    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    /**
     * IP address of the session.
     */
    @Column(name = "ip_address")
    private InetAddress ipAddress;

    /**
     * User agent string from the client.
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Timestamp when the session expires.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Timestamp when the session was created.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the session was last accessed.
     */
    @LastModifiedDate
    @Column(name = "last_accessed_at", nullable = false)
    private LocalDateTime lastAccessedAt;

    /**
     * Check if the session is expired.
     * 
     * @return true if session is expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if the session is active (not expired).
     * 
     * @return true if session is active, false otherwise
     */
    public boolean isActive() {
        return !isExpired();
    }

    /**
     * Update the last accessed timestamp.
     */
    public void updateLastAccessed() {
        this.lastAccessedAt = LocalDateTime.now();
    }

    /**
     * Extend the session expiration time.
     * 
     * @param minutes number of minutes to extend the session
     */
    public void extendSession(long minutes) {
        this.expiresAt = LocalDateTime.now().plusMinutes(minutes);
        updateLastAccessed();
    }
}
