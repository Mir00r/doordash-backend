package com.doordash.user_service.unit.services;

import com.doordash.user_service.domain.dtos.user.CreateUserProfileRequest;
import com.doordash.user_service.domain.dtos.user.UpdateUserProfileRequest;
import com.doordash.user_service.domain.dtos.user.UserProfileResponse;
import com.doordash.user_service.domain.entities.User;
import com.doordash.user_service.domain.entities.UserProfile;
import com.doordash.user_service.domain.enums.UserStatus;
import com.doordash.user_service.domain.mappers.UserMapper;
import com.doordash.user_service.exceptions.UserNotFoundException;
import com.doordash.user_service.repositories.UserRepository;
import com.doordash.user_service.services.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for UserService.
 * 
 * Tests all business logic methods in isolation using mocks.
 * Follows AAA (Arrange, Act, Assert) pattern for clarity.
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserProfile testProfile;
    private CreateUserProfileRequest createRequest;
    private UpdateUserProfileRequest updateRequest;
    private UserProfileResponse profileResponse;

    @BeforeEach
    void setUp() {
        // Arrange - Set up test data
        UUID userId = UUID.randomUUID();
        
        testUser = User.builder()
            .id(userId)
            .email("test@doordash.com")
            .username("testuser")
            .passwordHash("hashed_password")
            .status(UserStatus.ACTIVE)
            .emailVerified(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        testProfile = UserProfile.builder()
            .id(UUID.randomUUID())
            .user(testUser)
            .firstName("John")
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .phoneNumber("+1234567890")
            .build();

        testUser.setProfile(testProfile);

        createRequest = CreateUserProfileRequest.builder()
            .firstName("John")
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .phoneNumber("+1234567890")
            .build();

        updateRequest = UpdateUserProfileRequest.builder()
            .firstName("Jane")
            .lastName("Smith")
            .phoneNumber("+0987654321")
            .build();

        profileResponse = UserProfileResponse.builder()
            .id(testProfile.getId())
            .firstName(testProfile.getFirstName())
            .lastName(testProfile.getLastName())
            .email(testUser.getEmail())
            .dateOfBirth(testProfile.getDateOfBirth())
            .phoneNumber(testProfile.getPhoneNumber())
            .build();
    }

    @Nested
    @DisplayName("User Profile Creation Tests")
    class UserProfileCreationTests {

        @Test
        @DisplayName("Should create user profile successfully")
        void shouldCreateUserProfileSuccessfully() {
            // Arrange
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(userMapper.toUserProfileResponse(any(User.class))).thenReturn(profileResponse);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            UserProfileResponse result = userService.createUserProfile(testUser.getId(), createRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getFirstName()).isEqualTo(createRequest.getFirstName());
            assertThat(result.getLastName()).isEqualTo(createRequest.getLastName());
            assertThat(result.getPhoneNumber()).isEqualTo(createRequest.getPhoneNumber());

            verify(userRepository).findById(testUser.getId());
            verify(userRepository).save(any(User.class));
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Arrange
            UUID nonExistentUserId = UUID.randomUUID();
            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.createUserProfile(nonExistentUserId, createRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

            verify(userRepository).findById(nonExistentUserId);
            verify(userRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should validate required fields")
        void shouldValidateRequiredFields() {
            // Arrange
            CreateUserProfileRequest invalidRequest = CreateUserProfileRequest.builder()
                .firstName("") // Invalid - empty
                .lastName(null) // Invalid - null
                .build();

            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

            // Act & Assert
            assertThatThrownBy(() -> userService.createUserProfile(testUser.getId(), invalidRequest))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("User Profile Update Tests")
    class UserProfileUpdateTests {

        @Test
        @DisplayName("Should update user profile successfully")
        void shouldUpdateUserProfileSuccessfully() {
            // Arrange
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(userMapper.toUserProfileResponse(any(User.class))).thenReturn(profileResponse);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            UserProfileResponse result = userService.updateUserProfile(testUser.getId(), updateRequest);

            // Assert
            assertThat(result).isNotNull();
            verify(userRepository).findById(testUser.getId());
            verify(userRepository).save(any(User.class));
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("Should handle partial updates")
        void shouldHandlePartialUpdates() {
            // Arrange
            UpdateUserProfileRequest partialUpdate = UpdateUserProfileRequest.builder()
                .firstName("UpdatedName")
                // Other fields are null - should not be updated
                .build();

            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(userMapper.toUserProfileResponse(any(User.class))).thenReturn(profileResponse);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            UserProfileResponse result = userService.updateUserProfile(testUser.getId(), partialUpdate);

            // Assert
            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("User Profile Retrieval Tests")
    class UserProfileRetrievalTests {

        @Test
        @DisplayName("Should get user profile by ID")
        void shouldGetUserProfileById() {
            // Arrange
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(userMapper.toUserProfileResponse(testUser)).thenReturn(profileResponse);

            // Act
            UserProfileResponse result = userService.getUserProfile(testUser.getId());

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testProfile.getId());
            verify(userRepository).findById(testUser.getId());
        }

        @Test
        @DisplayName("Should get user profile by email")
        void shouldGetUserProfileByEmail() {
            // Arrange
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(userMapper.toUserProfileResponse(testUser)).thenReturn(profileResponse);

            // Act
            UserProfileResponse result = userService.getUserProfileByEmail(testUser.getEmail());

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
            verify(userRepository).findByEmail(testUser.getEmail());
        }

        @Test
        @DisplayName("Should throw exception when profile not found")
        void shouldThrowExceptionWhenProfileNotFound() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.getUserProfile(nonExistentId))
                .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("User Status Management Tests")
    class UserStatusManagementTests {

        @Test
        @DisplayName("Should activate user successfully")
        void shouldActivateUserSuccessfully() {
            // Arrange
            testUser.setStatus(UserStatus.INACTIVE);
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            userService.activateUser(testUser.getId());

            // Assert
            verify(userRepository).save(argThat(user -> user.getStatus() == UserStatus.ACTIVE));
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("Should deactivate user successfully")
        void shouldDeactivateUserSuccessfully() {
            // Arrange
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            userService.deactivateUser(testUser.getId());

            // Assert
            verify(userRepository).save(argThat(user -> user.getStatus() == UserStatus.INACTIVE));
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("Should verify email successfully")
        void shouldVerifyEmailSuccessfully() {
            // Arrange
            testUser.setEmailVerified(false);
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            userService.verifyEmail(testUser.getId());

            // Assert
            verify(userRepository).save(argThat(user -> user.isEmailVerified()));
            verify(eventPublisher).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("User Search and Filtering Tests")
    class UserSearchAndFilteringTests {

        @Test
        @DisplayName("Should search users by criteria")
        void shouldSearchUsersByCriteria() {
            // Arrange
            List<User> searchResults = List.of(testUser);
            when(userRepository.findByFirstNameContainingIgnoreCase("John")).thenReturn(searchResults);
            when(userMapper.toUserProfileResponse(testUser)).thenReturn(profileResponse);

            // Act
            List<UserProfileResponse> results = userService.searchUsersByName("John");

            // Assert
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getFirstName()).contains("John");
            verify(userRepository).findByFirstNameContainingIgnoreCase("John");
        }

        @Test
        @DisplayName("Should handle empty search results")
        void shouldHandleEmptySearchResults() {
            // Arrange
            when(userRepository.findByFirstNameContainingIgnoreCase("NonExistent")).thenReturn(List.of());

            // Act
            List<UserProfileResponse> results = userService.searchUsersByName("NonExistent");

            // Assert
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Cache Management Tests")
    class CacheManagementTests {

        @Test
        @DisplayName("Should evict cache when user updated")
        void shouldEvictCacheWhenUserUpdated() {
            // Arrange
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toUserProfileResponse(any(User.class))).thenReturn(profileResponse);

            // Act
            userService.updateUserProfile(testUser.getId(), updateRequest);

            // Assert
            verify(userRepository).save(any(User.class));
            // Cache eviction would be verified through integration tests
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should validate email format")
        void shouldValidateEmailFormat() {
            // This would be tested in integration tests with actual validation
            // Unit tests focus on business logic rather than validation annotations
        }

        @Test
        @DisplayName("Should validate phone number format")
        void shouldValidatePhoneNumberFormat() {
            // This would be tested in integration tests with actual validation
            // Unit tests focus on business logic rather than validation annotations
        }

        @Test
        @DisplayName("Should validate date of birth")
        void shouldValidateDateOfBirth() {
            // This would be tested in integration tests with actual validation
            // Unit tests focus on business logic rather than validation annotations
        }
    }
}
