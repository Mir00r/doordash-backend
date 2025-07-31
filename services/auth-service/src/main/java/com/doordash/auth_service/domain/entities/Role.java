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
 * Role entity representing a role in the role-based access control system.
 * 
 * Roles define collections of permissions that can be assigned to users.
 * This enables flexible authorization management where permissions can be
 * grouped logically and assigned to users through roles.
 * 
 * Example roles:
 * - ADMIN: Full system access
 * - CUSTOMER: Basic customer operations
 * - RESTAURANT_OWNER: Restaurant management
 * - DELIVERY_DRIVER: Delivery operations
 * 
 * @author DoorDash Engineering Team
 * @version 1.0.0
 * @since 2025-07-31
 */
@Entity
@Table(name = "roles")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    /**
     * Unique identifier for the role.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique name of the role (e.g., ADMIN, CUSTOMER, RESTAURANT_OWNER).
     * Used for role identification and assignment.
     */
    @Column(unique = true, nullable = false, length = 50)
    private String name;

    /**
     * Human-readable description of what this role represents.
     */
    @Column(length = 255)
    private String description;

    /**
     * Timestamp when the role was created.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the role was last updated.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * User-role associations for this role.
     * One role can be assigned to many users.
     */
    @Builder.Default
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserRole> userRoles = new HashSet<>();

    /**
     * Permissions granted to this role.
     * Many-to-many relationship with Permission entity.
     */
    @Builder.Default
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<RolePermission> rolePermissions = new HashSet<>();

    /**
     * Check if this role has a specific permission.
     * 
     * @param permissionName the name of the permission to check
     * @return true if the role has the permission, false otherwise
     */
    public boolean hasPermission(String permissionName) {
        return rolePermissions.stream()
                .anyMatch(rp -> rp.getPermission().getName().equals(permissionName));
    }

    /**
     * Get all permission names granted to this role.
     * 
     * @return set of permission names
     */
    public Set<String> getPermissionNames() {
        return rolePermissions.stream()
                .map(rp -> rp.getPermission().getName())
                .collect(java.util.stream.Collectors.toSet());
    }
}
