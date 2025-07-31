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
 * UserRole entity representing the many-to-many relationship between User and Role.
 * 
 * This junction table entity tracks when roles are assigned to users,
 * who assigned them, and provides audit trail for role assignments.
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@Entity
@Table(name = "user_roles")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {

    /**
     * Unique identifier for this user-role assignment.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who has been assigned the role.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The role that has been assigned to the user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * Timestamp when the role was assigned to the user.
     */
    @CreatedDate
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    /**
     * The user who assigned this role (for audit purposes).
     * Can be null for system-assigned roles.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;
}
