package com.doordash.user_service.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.time.Instant;
import java.util.Map;

/**
 * Test configuration for User Service integration tests.
 * Provides mock beans and test-specific configurations.
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@TestConfiguration
public class TestConfig {

    /**
     * Mock JWT decoder for testing JWT authentication flows.
     * Returns a valid test JWT token for authenticated endpoints.
     */
    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return token -> createMockJwt();
    }

    /**
     * In-memory user details manager for testing.
     * Provides test users with different roles and permissions.
     */
    @Bean
    @Primary
    public InMemoryUserDetailsManager userDetailsManager() {
        UserDetails user = User.withDefaultPasswordEncoder()
            .username("testuser")
            .password("password")
            .roles("USER")
            .build();
            
        UserDetails admin = User.withDefaultPasswordEncoder()
            .username("admin")
            .password("password")
            .roles("ADMIN", "USER")
            .build();
            
        UserDetails customer = User.withDefaultPasswordEncoder()
            .username("customer")
            .password("password")
            .roles("CUSTOMER")
            .build();
            
        return new InMemoryUserDetailsManager(user, admin, customer);
    }

    /**
     * Creates a mock JWT token for testing.
     * Contains standard claims used in authentication.
     */
    private Jwt createMockJwt() {
        return Jwt.withTokenValue("test-token")
            .header("alg", "HS256")
            .header("typ", "JWT")
            .claim("sub", "test-user-123")
            .claim("email", "test@doordash.com")
            .claim("preferred_username", "testuser")
            .claim("given_name", "Test")
            .claim("family_name", "User")
            .claim("roles", "USER")
            .claim("exp", Instant.now().plusSeconds(3600))
            .claim("iat", Instant.now())
            .build();
    }
}
