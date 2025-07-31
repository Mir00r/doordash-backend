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
 * AuditLog entity for tracking security-related events and user actions.
 * 
 * This entity provides a comprehensive audit trail for the authentication
 * system, tracking login attempts, permission changes, and other security-
 * relevant events for compliance and security monitoring.
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    /**
     * Unique identifier for the audit log entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user associated with this audit event.
     * Can be null for system events or failed login attempts.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The action that was performed (e.g., LOGIN, LOGOUT, PASSWORD_CHANGE).
     */
    @Column(nullable = false, length = 100)
    private String action;

    /**
     * The resource that was affected (e.g., user, role, permission).
     */
    @Column(length = 100)
    private String resource;

    /**
     * The ID of the specific resource that was affected.
     */
    @Column(name = "resource_id", length = 100)
    private String resourceId;

    /**
     * Additional details about the event in JSON format.
     * Can include old/new values, error messages, etc.
     */
    @Column(columnDefinition = "JSONB")
    private String details;

    /**
     * IP address from which the action was performed.
     */
    @Column(name = "ip_address")
    private InetAddress ipAddress;

    /**
     * User agent string from the client.
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Status of the action (SUCCESS, FAILURE, ERROR).
     */
    @Column(nullable = false, length = 20)
    private String status;

    /**
     * Timestamp when the event occurred.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Audit log status enumeration.
     */
    public enum Status {
        SUCCESS,
        FAILURE,
        ERROR
    }

    /**
     * Common audit actions enumeration.
     */
    public enum Action {
        LOGIN,
        LOGOUT,
        LOGIN_FAILED,
        ACCOUNT_LOCKED,
        PASSWORD_CHANGE,
        PASSWORD_RESET_REQUEST,
        PASSWORD_RESET,
        EMAIL_VERIFICATION,
        ROLE_ASSIGNED,
        ROLE_REVOKED,
        PERMISSION_GRANTED,
        PERMISSION_REVOKED,
        ACCOUNT_CREATED,
        ACCOUNT_UPDATED,
        ACCOUNT_DELETED,
        TOKEN_ISSUED,
        TOKEN_REFRESHED,
        TOKEN_REVOKED,
        OAUTH_LINK,
        OAUTH_UNLINK
    }
}
