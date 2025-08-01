package com.doordash.user_service.security.authentication;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Collection;

/**
 * Custom JWT Authentication Token for DoorDash User Service.
 * 
 * Extends Spring Security's JwtAuthenticationToken to include additional
 * user context and DoorDash-specific claims for comprehensive authentication
 * and authorization in a microservices architecture.
 * 
 * Features:
 * - Enhanced user context with DoorDash-specific attributes
 * - Service-to-service authentication support
 * - Multi-tenant architecture support
 * - Rich authentication metadata for audit and logging
 * - Integration with DoorDash security model
 * 
 * Additional Context:
 * - User ID and tenant information
 * - User type classification (CUSTOMER, DRIVER, etc.)
 * - Service authentication details
 * - Token lifecycle information
 * - Request correlation data
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
public class DoorDashJwtAuthenticationToken extends JwtAuthenticationToken {

    private final DoorDashUserDetails userDetails;

    /**
     * Constructs a DoorDash JWT Authentication Token.
     * 
     * @param jwt the JWT token
     * @param authorities the granted authorities
     * @param userDetails the DoorDash user details
     */
    public DoorDashJwtAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities, DoorDashUserDetails userDetails) {
        super(jwt, authorities, userDetails.getUsername());
        this.userDetails = userDetails;
    }

    /**
     * Gets the user ID from the authentication token.
     * 
     * @return String the user ID
     */
    public String getUserId() {
        return userDetails.getUserId();
    }

    /**
     * Gets the tenant ID from the authentication token.
     * 
     * @return String the tenant ID
     */
    public String getTenantId() {
        return userDetails.getTenantId();
    }

    /**
     * Gets the user type from the authentication token.
     * 
     * @return String the user type (CUSTOMER, DRIVER, RESTAURANT_OWNER, etc.)
     */
    public String getUserType() {
        return userDetails.getUserType();
    }

    /**
     * Gets the service name for service-to-service authentication.
     * 
     * @return String the service name
     */
    public String getServiceName() {
        return userDetails.getServiceName();
    }

    /**
     * Checks if this is a service-to-service authentication.
     * 
     * @return boolean true if service authentication
     */
    public boolean isServiceAuthentication() {
        return userDetails.getServiceName() != null;
    }

    /**
     * Gets the complete user details.
     * 
     * @return DoorDashUserDetails the user details
     */
    public DoorDashUserDetails getUserDetails() {
        return userDetails;
    }

    /**
     * Gets the preferred username from the token.
     * 
     * @return String the preferred username
     */
    public String getPreferredUsername() {
        return userDetails.getPreferredUsername();
    }

    /**
     * Gets the token issued at timestamp.
     * 
     * @return Instant the issued at timestamp
     */
    public Instant getIssuedAt() {
        return userDetails.getIssuedAt();
    }

    /**
     * Gets the token expiration timestamp.
     * 
     * @return Instant the expiration timestamp
     */
    public Instant getExpiresAt() {
        return userDetails.getExpiresAt();
    }
}

/**
 * DoorDash User Details for comprehensive user context.
 * 
 * Contains all user-related information extracted from JWT claims
 * for use throughout the application for authorization decisions,
 * audit logging, and business logic.
 */
@Data
@Builder
class DoorDashUserDetails {
    /**
     * Unique user identifier.
     */
    private String userId;

    /**
     * Username for authentication.
     */
    private String username;

    /**
     * Preferred username for display.
     */
    private String preferredUsername;

    /**
     * Tenant identifier for multi-tenant support.
     */
    private String tenantId;

    /**
     * User type classification.
     */
    private String userType;

    /**
     * Service name for service-to-service authentication.
     */
    private String serviceName;

    /**
     * Token issued at timestamp.
     */
    private Instant issuedAt;

    /**
     * Token expiration timestamp.
     */
    private Instant expiresAt;
}
