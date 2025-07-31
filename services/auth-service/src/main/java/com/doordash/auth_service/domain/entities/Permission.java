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
 * Permission entity representing a specific permission in the system.
 * 
 * Permissions define granular access rights to resources and actions.
 * They follow a structured naming convention: resource.action
 * (e.g., user.read, order.write, restaurant.manage).
 * 
 * Permissions are granted to roles, which are then assigned to users,
 * implementing a role-based access control (RBAC) system.
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@Entity
@Table(name = "permissions")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    /**
     * Unique identifier for the permission.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique name of the permission following resource.action convention.
     * Examples: user.read, order.write, restaurant.manage
     */
    @Column(unique = true, nullable = false, length = 100)
    private String name;

    /**
     * The resource this permission applies to (e.g., user, order, restaurant).
     */
    @Column(nullable = false, length = 100)
    private String resource;

    /**
     * The action this permission allows (e.g., read, write, delete, manage).
     */
    @Column(nullable = false, length = 50)
    private String action;

    /**
     * Human-readable description of what this permission grants.
     */
    @Column(length = 255)
    private String description;

    /**
     * Timestamp when the permission was created.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the permission was last updated.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Role-permission associations for this permission.
     * One permission can be granted to many roles.
     */
    @Builder.Default
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<RolePermission> rolePermissions = new HashSet<>();

    /**
     * Get the full permission string in resource.action format.
     * 
     * @return formatted permission string
     */
    public String getFullPermission() {
        return resource + "." + action;
    }

    /**
     * Check if this permission matches the given resource and action.
     * 
     * @param resource the resource to check
     * @param action the action to check
     * @return true if matches, false otherwise
     */
    public boolean matches(String resource, String action) {
        return this.resource.equals(resource) && this.action.equals(action);
    }
}
