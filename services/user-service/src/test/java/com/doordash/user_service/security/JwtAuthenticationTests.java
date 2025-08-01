package com.doordash.user_service.security;

import com.doordash.user_service.security.jwt.CustomJwtAuthenticationConverter;
import com.doordash.user_service.security.jwt.DoorDashJwtAuthenticationToken;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit Tests for JWT Authentication Components.
 * 
 * Tests the JWT authentication functionality including:
 * - JWT token conversion and validation
 * - Claims extraction and processing
 * - Authority mapping from token claims
 * - Custom authentication token creation
 * - Permission and role handling
 * - Error handling for malformed tokens
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Authentication Component Tests")
class JwtAuthenticationTests {

    private CustomJwtAuthenticationConverter jwtAuthenticationConverter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationConverter = new CustomJwtAuthenticationConverter();
    }

    @Test
    @DisplayName("Should convert JWT with user claims to DoorDash authentication token")
    void shouldConvertJwtWithUserClaimsToDoorDashAuthenticationToken() {
        // Given
        Map<String, Object> headers = Map.of("alg", "RS256", "typ", "JWT");
        Map<String, Object> claims = Map.of(
            "sub", "user123",
            "user_id", "user123",
            "email", "john.doe@doordash.com",
            "first_name", "John",
            "last_name", "Doe",
            "roles", List.of("USER", "CUSTOMER"),
            "permissions", List.of("READ_PROFILE", "UPDATE_PROFILE", "PLACE_ORDER"),
            "service_name", "user-service",
            "aud", "doordash-api",
            "iss", "https://auth.doordash.com",
            "iat", Instant.now().getEpochSecond(),
            "exp", Instant.now().plusSeconds(3600).getEpochSecond()
        );

        Jwt jwt = new Jwt("mock-token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        // When
        Authentication authentication = jwtAuthenticationConverter.convert(jwt);

        // Then
        assertThat(authentication).isInstanceOf(DoorDashJwtAuthenticationToken.class);
        
        DoorDashJwtAuthenticationToken token = (DoorDashJwtAuthenticationToken) authentication;
        assertThat(token.getName()).isEqualTo("john.doe@doordash.com");
        assertThat(token.getUserId()).isEqualTo("user123");
        assertThat(token.getEmail()).isEqualTo("john.doe@doordash.com");
        assertThat(token.getFirstName()).isEqualTo("John");
        assertThat(token.getLastName()).isEqualTo("Doe");
        assertThat(token.getServiceName()).isEqualTo("user-service");
        assertThat(token.isAuthenticated()).isTrue();

        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertThat(authorities).hasSize(5); // 2 roles + 3 permissions
        assertThat(authorities).containsExactlyInAnyOrder(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_CUSTOMER"),
            new SimpleGrantedAuthority("READ_PROFILE"),
            new SimpleGrantedAuthority("UPDATE_PROFILE"),
            new SimpleGrantedAuthority("PLACE_ORDER")
        );
    }

    @Test
    @DisplayName("Should convert JWT with service claims to service authentication token")
    void shouldConvertJwtWithServiceClaimsToServiceAuthenticationToken() {
        // Given
        Map<String, Object> headers = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> claims = Map.of(
            "sub", "service:auth-service",
            "service_name", "auth-service",
            "service_version", "1.0.0",
            "scopes", List.of("user:read", "user:write", "auth:validate"),
            "aud", "doordash-internal",
            "iss", "https://auth.doordash.com",
            "iat", Instant.now().getEpochSecond(),
            "exp", Instant.now().plusSeconds(3600).getEpochSecond()
        );

        Jwt jwt = new Jwt("mock-service-token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        // When
        Authentication authentication = jwtAuthenticationConverter.convert(jwt);

        // Then
        assertThat(authentication).isInstanceOf(DoorDashJwtAuthenticationToken.class);
        
        DoorDashJwtAuthenticationToken token = (DoorDashJwtAuthenticationToken) authentication;
        assertThat(token.getName()).isEqualTo("service:auth-service");
        assertThat(token.getServiceName()).isEqualTo("auth-service");
        assertThat(token.getUserId()).isNull();
        assertThat(token.getEmail()).isNull();
        assertThat(token.isAuthenticated()).isTrue();

        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertThat(authorities).hasSize(4); // 1 service role + 3 scopes
        assertThat(authorities).containsExactlyInAnyOrder(
            new SimpleGrantedAuthority("ROLE_SERVICE"),
            new SimpleGrantedAuthority("user:read"),
            new SimpleGrantedAuthority("user:write"),
            new SimpleGrantedAuthority("auth:validate")
        );
    }

    @Test
    @DisplayName("Should handle JWT with minimal claims")
    void shouldHandleJwtWithMinimalClaims() {
        // Given
        Map<String, Object> headers = Map.of("alg", "RS256", "typ", "JWT");
        Map<String, Object> claims = Map.of(
            "sub", "user456",
            "aud", "doordash-api",
            "iss", "https://auth.doordash.com",
            "iat", Instant.now().getEpochSecond(),
            "exp", Instant.now().plusSeconds(3600).getEpochSecond()
        );

        Jwt jwt = new Jwt("minimal-token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        // When
        Authentication authentication = jwtAuthenticationConverter.convert(jwt);

        // Then
        assertThat(authentication).isInstanceOf(DoorDashJwtAuthenticationToken.class);
        
        DoorDashJwtAuthenticationToken token = (DoorDashJwtAuthenticationToken) authentication;
        assertThat(token.getName()).isEqualTo("user456");
        assertThat(token.getUserId()).isEqualTo("user456");
        assertThat(token.getEmail()).isNull();
        assertThat(token.getFirstName()).isNull();
        assertThat(token.getLastName()).isNull();
        assertThat(token.getServiceName()).isNull();
        assertThat(token.isAuthenticated()).isTrue();

        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertThat(authorities).hasSize(1); // Default USER role
        assertThat(authorities).contains(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    @DisplayName("Should extract authorities from both roles and permissions")
    void shouldExtractAuthoritiesFromBothRolesAndPermissions() {
        // Given
        Map<String, Object> headers = Map.of("alg", "RS256", "typ", "JWT");
        Map<String, Object> claims = Map.of(
            "sub", "admin123",
            "roles", List.of("ADMIN", "MODERATOR"),
            "permissions", List.of("MANAGE_USERS", "MANAGE_RESTAURANTS", "VIEW_ANALYTICS"),
            "scopes", List.of("admin:full", "analytics:read"),
            "aud", "doordash-api",
            "iss", "https://auth.doordash.com",
            "iat", Instant.now().getEpochSecond(),
            "exp", Instant.now().plusSeconds(3600).getEpochSecond()
        );

        Jwt jwt = new Jwt("admin-token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        // When
        Authentication authentication = jwtAuthenticationConverter.convert(jwt);

        // Then
        assertThat(authentication).isInstanceOf(DoorDashJwtAuthenticationToken.class);
        
        DoorDashJwtAuthenticationToken token = (DoorDashJwtAuthenticationToken) authentication;
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        
        assertThat(authorities).hasSize(7); // 2 roles + 3 permissions + 2 scopes
        assertThat(authorities).containsExactlyInAnyOrder(
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("ROLE_MODERATOR"),
            new SimpleGrantedAuthority("MANAGE_USERS"),
            new SimpleGrantedAuthority("MANAGE_RESTAURANTS"),
            new SimpleGrantedAuthority("VIEW_ANALYTICS"),
            new SimpleGrantedAuthority("admin:full"),
            new SimpleGrantedAuthority("analytics:read")
        );
    }

    @Test
    @DisplayName("Should handle different claim types gracefully")
    void shouldHandleDifferentClaimTypesGracefully() {
        // Given
        Map<String, Object> headers = Map.of("alg", "RS256", "typ", "JWT");
        Map<String, Object> claims = Map.of(
            "sub", "user789",
            "roles", "SINGLE_ROLE", // String instead of List
            "permissions", Arrays.asList("PERM1", "PERM2"), // Array instead of List
            "aud", "doordash-api",
            "iss", "https://auth.doordash.com",
            "iat", Instant.now().getEpochSecond(),
            "exp", Instant.now().plusSeconds(3600).getEpochSecond()
        );

        Jwt jwt = new Jwt("flexible-token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        // When
        Authentication authentication = jwtAuthenticationConverter.convert(jwt);

        // Then
        assertThat(authentication).isInstanceOf(DoorDashJwtAuthenticationToken.class);
        
        DoorDashJwtAuthenticationToken token = (DoorDashJwtAuthenticationToken) authentication;
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        
        assertThat(authorities).hasSize(3); // 1 role + 2 permissions
        assertThat(authorities).containsExactlyInAnyOrder(
            new SimpleGrantedAuthority("ROLE_SINGLE_ROLE"),
            new SimpleGrantedAuthority("PERM1"),
            new SimpleGrantedAuthority("PERM2")
        );
    }

    @Test
    @DisplayName("Should create DoorDash JWT authentication token with all properties")
    void shouldCreateDoorDashJwtAuthenticationTokenWithAllProperties() {
        // Given
        Map<String, Object> headers = Map.of("alg", "RS256", "typ", "JWT");
        Map<String, Object> claims = Map.of(
            "sub", "test123",
            "user_id", "test123",
            "email", "test@doordash.com",
            "first_name", "Test",
            "last_name", "User",
            "service_name", "user-service",
            "aud", "doordash-api",
            "iss", "https://auth.doordash.com",
            "iat", Instant.now().getEpochSecond(),
            "exp", Instant.now().plusSeconds(3600).getEpochSecond()
        );

        Jwt jwt = new Jwt("test-token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
        Collection<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("READ_PROFILE")
        );

        // When
        DoorDashJwtAuthenticationToken token = new DoorDashJwtAuthenticationToken(jwt, authorities);

        // Then
        assertThat(token.getToken()).isEqualTo(jwt);
        assertThat(token.getAuthorities()).isEqualTo(authorities);
        assertThat(token.getCredentials()).isEqualTo(jwt);
        assertThat(token.getPrincipal()).isEqualTo(jwt);
        assertThat(token.getName()).isEqualTo("test@doordash.com");
        assertThat(token.getUserId()).isEqualTo("test123");
        assertThat(token.getEmail()).isEqualTo("test@doordash.com");
        assertThat(token.getFirstName()).isEqualTo("Test");
        assertThat(token.getLastName()).isEqualTo("User");
        assertThat(token.getServiceName()).isEqualTo("user-service");
        assertThat(token.isAuthenticated()).isTrue();
    }

    @Test
    @DisplayName("Should handle token without user_id claim")
    void shouldHandleTokenWithoutUserIdClaim() {
        // Given
        Map<String, Object> headers = Map.of("alg", "RS256", "typ", "JWT");
        Map<String, Object> claims = Map.of(
            "sub", "external123",
            "email", "external@partner.com",
            "aud", "doordash-api",
            "iss", "https://partner.auth.com",
            "iat", Instant.now().getEpochSecond(),
            "exp", Instant.now().plusSeconds(3600).getEpochSecond()
        );

        Jwt jwt = new Jwt("external-token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        // When
        Authentication authentication = jwtAuthenticationConverter.convert(jwt);

        // Then
        assertThat(authentication).isInstanceOf(DoorDashJwtAuthenticationToken.class);
        
        DoorDashJwtAuthenticationToken token = (DoorDashJwtAuthenticationToken) authentication;
        assertThat(token.getUserId()).isEqualTo("external123"); // Falls back to subject
        assertThat(token.getEmail()).isEqualTo("external@partner.com");
        assertThat(token.getName()).isEqualTo("external@partner.com");
    }

    @Test
    @DisplayName("Should prioritize email over subject for name")
    void shouldPrioritizeEmailOverSubjectForName() {
        // Given
        Map<String, Object> headers = Map.of("alg", "RS256", "typ", "JWT");
        Map<String, Object> claims = Map.of(
            "sub", "uuid-12345",
            "email", "preferred@doordash.com",
            "aud", "doordash-api",
            "iss", "https://auth.doordash.com",
            "iat", Instant.now().getEpochSecond(),
            "exp", Instant.now().plusSeconds(3600).getEpochSecond()
        );

        Jwt jwt = new Jwt("preference-token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        // When
        Authentication authentication = jwtAuthenticationConverter.convert(jwt);

        // Then
        assertThat(authentication).isInstanceOf(DoorDashJwtAuthenticationToken.class);
        
        DoorDashJwtAuthenticationToken token = (DoorDashJwtAuthenticationToken) authentication;
        assertThat(token.getName()).isEqualTo("preferred@doordash.com");
        assertThat(token.getUserId()).isEqualTo("uuid-12345");
    }

    @Test
    @DisplayName("Should handle empty roles and permissions lists")
    void shouldHandleEmptyRolesAndPermissionsLists() {
        // Given
        Map<String, Object> headers = Map.of("alg", "RS256", "typ", "JWT");
        Map<String, Object> claims = Map.of(
            "sub", "empty123",
            "roles", Collections.emptyList(),
            "permissions", Collections.emptyList(),
            "aud", "doordash-api",
            "iss", "https://auth.doordash.com",
            "iat", Instant.now().getEpochSecond(),
            "exp", Instant.now().plusSeconds(3600).getEpochSecond()
        );

        Jwt jwt = new Jwt("empty-token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        // When
        Authentication authentication = jwtAuthenticationConverter.convert(jwt);

        // Then
        assertThat(authentication).isInstanceOf(DoorDashJwtAuthenticationToken.class);
        
        DoorDashJwtAuthenticationToken token = (DoorDashJwtAuthenticationToken) authentication;
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        
        assertThat(authorities).hasSize(1); // Default USER role
        assertThat(authorities).contains(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    @DisplayName("Should extract full name when available")
    void shouldExtractFullNameWhenAvailable() {
        // Given
        Map<String, Object> headers = Map.of("alg", "RS256", "typ", "JWT");
        Map<String, Object> claims = Map.of(
            "sub", "fullname123",
            "first_name", "Jane",
            "last_name", "Smith",
            "aud", "doordash-api",
            "iss", "https://auth.doordash.com",
            "iat", Instant.now().getEpochSecond(),
            "exp", Instant.now().plusSeconds(3600).getEpochSecond()
        );

        Jwt jwt = new Jwt("fullname-token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        // When
        Authentication authentication = jwtAuthenticationConverter.convert(jwt);

        // Then
        assertThat(authentication).isInstanceOf(DoorDashJwtAuthenticationToken.class);
        
        DoorDashJwtAuthenticationToken token = (DoorDashJwtAuthenticationToken) authentication;
        assertThat(token.getFirstName()).isEqualTo("Jane");
        assertThat(token.getLastName()).isEqualTo("Smith");
        assertThat(token.getFullName()).isEqualTo("Jane Smith");
    }

    @Test
    @DisplayName("Should handle partial name information")
    void shouldHandlePartialNameInformation() {
        // Given
        Map<String, Object> headers = Map.of("alg", "RS256", "typ", "JWT");
        Map<String, Object> claims = Map.of(
            "sub", "partial123",
            "first_name", "OnlyFirst",
            "aud", "doordash-api",
            "iss", "https://auth.doordash.com",
            "iat", Instant.now().getEpochSecond(),
            "exp", Instant.now().plusSeconds(3600).getEpochSecond()
        );

        Jwt jwt = new Jwt("partial-token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        // When
        Authentication authentication = jwtAuthenticationConverter.convert(jwt);

        // Then
        assertThat(authentication).isInstanceOf(DoorDashJwtAuthenticationToken.class);
        
        DoorDashJwtAuthenticationToken token = (DoorDashJwtAuthenticationToken) authentication;
        assertThat(token.getFirstName()).isEqualTo("OnlyFirst");
        assertThat(token.getLastName()).isNull();
        assertThat(token.getFullName()).isEqualTo("OnlyFirst");
    }
}
