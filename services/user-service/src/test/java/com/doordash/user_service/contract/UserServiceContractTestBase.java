package com.doordash.user_service.contract;

import com.doordash.user_service.domain.dtos.user.UserProfileResponse;
import com.doordash.user_service.services.UserService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Base class for Spring Cloud Contract tests.
 * 
 * Defines the contract testing setup for User Service.
 * This class is used by auto-generated contract tests.
 * 
 * Contract tests ensure API compatibility between services
 * and prevent breaking changes in inter-service communication.
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("contract-test")
@DirtiesContext
@AutoConfigureMessageVerifier
public abstract class UserServiceContractTestBase {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private UserService userService;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
        
        // Setup mock responses for contract tests
        setupUserServiceMocks();
    }

    private void setupUserServiceMocks() {
        // Mock successful user profile retrieval
        UserProfileResponse mockProfile = UserProfileResponse.builder()
            .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@doordash.com")
            .phoneNumber("+1234567890")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .build();

        when(userService.getUserProfile(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")))
            .thenReturn(mockProfile);

        // Mock user not found scenario
        when(userService.getUserProfile(UUID.fromString("00000000-0000-0000-0000-000000000000")))
            .thenThrow(new com.doordash.user_service.exceptions.UserNotFoundException("User not found"));

        // Mock profile creation
        when(userService.createUserProfile(any(UUID.class), any()))
            .thenReturn(mockProfile);

        // Mock profile update
        when(userService.updateUserProfile(any(UUID.class), any()))
            .thenReturn(mockProfile);

        // Mock user search
        when(userService.searchUsersByName("John"))
            .thenReturn(java.util.List.of(mockProfile));

        // Mock empty search results
        when(userService.searchUsersByName("NonExistent"))
            .thenReturn(java.util.List.of());
    }
}
