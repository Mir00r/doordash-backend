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
 * RolePermission entity representing the many-to-many relationship between Role and Permission.
 * 
 * This junction table entity tracks which permissions are granted to which roles,
 * when they were granted, and who granted them for audit purposes.
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@Entity
@Table(name = "role_permissions")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermission {

    /**
     * Unique identifier for this role-permission grant.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The role that has been granted the permission.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * The permission that has been granted to the role.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    /**
     * Timestamp when the permission was granted to the role.
     */
    @CreatedDate
    @Column(name = "granted_at", nullable = false, updatable = false)
    private LocalDateTime grantedAt;

    /**
     * The user who granted this permission (for audit purposes).
     * Can be null for system-granted permissions.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by")
    private User grantedBy;
}
