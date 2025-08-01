package com.doordash.user_service.security;

import com.doordash.user_service.config.SecurityProperties;
import com.doordash.user_service.security.authorization.DoorDashPermissionEvaluator;
import com.doordash.user_service.security.jwt.DoorDashJwtAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit Tests for Permission-Based Authorization.
 * 
 * Tests the comprehensive authorization functionality including:
 * - Permission-based access control
 * - Resource-level authorization
 * - Role-based authorization
 * - Service-to-service authorization
 * - Domain object authorization
 * - Method-level security integration
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Permission-Based Authorization Tests")
class PermissionBasedAuthorizationTests {

    @Mock
    private SecurityProperties securityProperties;

    private DoorDashPermissionEvaluator permissionEvaluator;

    @BeforeEach
    void setUp() {
        permissionEvaluator = new DoorDashPermissionEvaluator(securityProperties);
    }

    @Test
    @DisplayName("Should allow access with exact permission match")
    void shouldAllowAccessWithExactPermissionMatch() {
        // Given
        Authentication authentication = createUserAuthentication(
            "user123",
            List.of("READ_PROFILE", "UPDATE_PROFILE")
        );

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication, null, "READ_PROFILE"
        );

        // Then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("Should deny access without required permission")
    void shouldDenyAccessWithoutRequiredPermission() {
        // Given
        Authentication authentication = createUserAuthentication(
            "user123",
            List.of("READ_PROFILE")
        );

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication, null, "DELETE_PROFILE"
        );

        // Then
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("Should allow resource-level authorization for owner")
    void shouldAllowResourceLevelAuthorizationForOwner() {
        // Given
        Authentication authentication = createUserAuthentication(
            "user123",
            List.of("READ_PROFILE")
        );
        
        UserProfile userProfile = new UserProfile("user123", "john@doordash.com");

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication, userProfile, "READ"
        );

        // Then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("Should deny resource-level authorization for non-owner")
    void shouldDenyResourceLevelAuthorizationForNonOwner() {
        // Given
        Authentication authentication = createUserAuthentication(
            "user123",
            List.of("READ_PROFILE")
        );
        
        UserProfile userProfile = new UserProfile("user456", "jane@doordash.com");

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication, userProfile, "READ"
        );

        // Then
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("Should allow admin access to any resource")
    void shouldAllowAdminAccessToAnyResource() {
        // Given
        Authentication authentication = createUserAuthentication(
            "admin123",
            List.of("ROLE_ADMIN", "MANAGE_USERS")
        );
        
        UserProfile userProfile = new UserProfile("user456", "jane@doordash.com");

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication, userProfile, "READ"
        );

        // Then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("Should handle service-to-service authorization")
    void shouldHandleServiceToServiceAuthorization() {
        // Given
        Authentication authentication = createServiceAuthentication(
            "auth-service",
            List.of("user:read", "user:write")
        );

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication, null, "user:read"
        );

        // Then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("Should deny service access without proper scope")
    void shouldDenyServiceAccessWithoutProperScope() {
        // Given
        Authentication authentication = createServiceAuthentication(
            "notification-service",
            List.of("notification:send")
        );

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication, null, "user:write"
        );

        // Then
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("Should support hierarchical permission checking")
    void shouldSupportHierarchicalPermissionChecking() {
        // Given
        Authentication authentication = createUserAuthentication(
            "manager123",
            List.of("MANAGE_RESTAURANT_ORDERS")
        );

        // When
        boolean hasViewPermission = permissionEvaluator.hasPermission(
            authentication, null, "VIEW_RESTAURANT_ORDERS"
        );
        boolean hasManagePermission = permissionEvaluator.hasPermission(
            authentication, null, "MANAGE_RESTAURANT_ORDERS"
        );

        // Then
        assertThat(hasViewPermission).isTrue(); // Implied by manage permission
        assertThat(hasManagePermission).isTrue();
    }

    @Test
    @DisplayName("Should handle resource authorization by ID")
    void shouldHandleResourceAuthorizationById() {
        // Given
        Authentication authentication = createUserAuthentication(
            "user123",
            List.of("READ_ORDER")
        );

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication, "order-456", "com.doordash.Order", "READ"
        );

        // Then
        // This would typically involve a service call to check ownership
        // For this test, we'll assume the permission evaluator can handle it
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("Should handle null authentication gracefully")
    void shouldHandleNullAuthenticationGracefully() {
        // Given
        Authentication authentication = null;

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication, null, "READ_PROFILE"
        );

        // Then
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("Should handle anonymous authentication")
    void shouldHandleAnonymousAuthentication() {
        // Given
        Authentication authentication = createAnonymousAuthentication();

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication, null, "PUBLIC_ACCESS"
        );

        // Then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("Should deny anonymous access to protected resources")
    void shouldDenyAnonymousAccessToProtectedResources() {
        // Given
        Authentication authentication = createAnonymousAuthentication();

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication, null, "READ_PROFILE"
        );

        // Then
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("Should support context-aware authorization")
    void shouldSupportContextAwareAuthorization() {
        // Given
        Authentication authentication = createUserAuthentication(
            "user123",
            List.of("UPDATE_PROFILE")
        );
        
        Map<String, Object> context = Map.of(
            "resource_owner", "user123",
            "action_type", "UPDATE",
            "field", "email"
        );

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication, context, "UPDATE_EMAIL"
        );

        // Then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("Should handle complex authorization scenarios")
    void shouldHandleComplexAuthorizationScenarios() {
        // Given
        Authentication authentication = createUserAuthentication(
            "restaurant-owner-123",
            List.of("ROLE_RESTAURANT_OWNER", "MANAGE_RESTAURANT", "VIEW_ORDERS")
        );
        
        RestaurantOrder order = new RestaurantOrder("order-789", "restaurant-456");
        
        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication, order, "VIEW"
        );

        // Then
        // Should check if user owns the restaurant that the order belongs to
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("Should support time-based permissions")
    void shouldSupportTimeBasedPermissions() {
        // Given
        Authentication authentication = createUserAuthentication(
            "user123",
            List.of("TEMPORARY_ACCESS")
        );

        // When
        boolean hasPermission = permissionEvaluator.hasPermission(
            authentication, Map.of("valid_until", Instant.now().plusSeconds(3600)), "ACCESS_RESOURCE"
        );

        // Then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("Should handle multiple permission requirements")
    void shouldHandleMultiplePermissionRequirements() {
        // Given
        Authentication authentication = createUserAuthentication(
            "user123",
            List.of("READ_PROFILE", "WRITE_PROFILE", "MANAGE_ORDERS")
        );

        // When
        boolean hasAllPermissions = 
            permissionEvaluator.hasPermission(authentication, null, "READ_PROFILE") &&
            permissionEvaluator.hasPermission(authentication, null, "WRITE_PROFILE") &&
            permissionEvaluator.hasPermission(authentication, null, "MANAGE_ORDERS");

        // Then
        assertThat(hasAllPermissions).isTrue();
    }

    // Helper methods for creating test authentication objects

    private Authentication createUserAuthentication(String userId, List<String> authorities) {
        Map<String, Object> headers = Map.of("alg", "RS256", "typ", "JWT");
        Map<String, Object> claims = Map.of(
            "sub", userId,
            "user_id", userId,
            "email", userId + "@doordash.com",
            "aud", "doordash-api",
            "iss", "https://auth.doordash.com",
            "iat", Instant.now().getEpochSecond(),
            "exp", Instant.now().plusSeconds(3600).getEpochSecond()
        );

        Jwt jwt = new Jwt("test-token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
        
        Collection<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
            .map(auth -> auth.startsWith("ROLE_") ? 
                new SimpleGrantedAuthority(auth) : 
                new SimpleGrantedAuthority(auth))
            .toList();

        return new DoorDashJwtAuthenticationToken(jwt, grantedAuthorities);
    }

    private Authentication createServiceAuthentication(String serviceName, List<String> scopes) {
        Map<String, Object> headers = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> claims = Map.of(
            "sub", "service:" + serviceName,
            "service_name", serviceName,
            "aud", "doordash-internal",
            "iss", "https://auth.doordash.com",
            "iat", Instant.now().getEpochSecond(),
            "exp", Instant.now().plusSeconds(3600).getEpochSecond()
        );

        Jwt jwt = new Jwt("service-token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
        
        Collection<SimpleGrantedAuthority> grantedAuthorities = scopes.stream()
            .map(SimpleGrantedAuthority::new)
            .toList();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_SERVICE"));

        return new DoorDashJwtAuthenticationToken(jwt, grantedAuthorities);
    }

    private Authentication createAnonymousAuthentication() {
        Map<String, Object> headers = Map.of("alg", "none", "typ", "JWT");
        Map<String, Object> claims = Map.of(
            "sub", "anonymous",
            "aud", "doordash-api",
            "iss", "https://auth.doordash.com",
            "iat", Instant.now().getEpochSecond(),
            "exp", Instant.now().plusSeconds(3600).getEpochSecond()
        );

        Jwt jwt = new Jwt("anonymous-token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
        
        return new DoorDashJwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    }

    // Test domain objects

    private static class UserProfile {
        private final String userId;
        private final String email;

        public UserProfile(String userId, String email) {
            this.userId = userId;
            this.email = email;
        }

        public String getUserId() { return userId; }
        public String getEmail() { return email; }
    }

    private static class RestaurantOrder {
        private final String orderId;
        private final String restaurantId;

        public RestaurantOrder(String orderId, String restaurantId) {
            this.orderId = orderId;
            this.restaurantId = restaurantId;
        }

        public String getOrderId() { return orderId; }
        public String getRestaurantId() { return restaurantId; }
    }
}
