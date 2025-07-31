package com.doordash.user_service.controllers;

import com.doordash.user_service.domain.dtos.user.*;
import com.doordash.user_service.domain.entities.UserProfile;
import com.doordash.user_service.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for user profile management.
 * 
 * Provides comprehensive user profile operations including CRUD operations,
 * search functionality, file uploads, and profile verification.
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Profile Management", description = "APIs for managing user profiles and personal information")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create user profile", description = "Create a new user profile for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User profile created successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "User profile already exists",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/profiles")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> createProfile(
            @Valid @RequestBody CreateUserProfileRequest request,
            Authentication authentication) {
        
        log.info("Creating user profile for user: {}", request.getUserId());
        
        // Ensure user can only create their own profile (unless admin)
        String userIdFromToken = authentication.getName();
        if (!request.getUserId().toString().equals(userIdFromToken) && 
            !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        request.sanitize();
        UserProfileResponse response = userService.createProfile(request);
        
        log.info("User profile created successfully for user: {}", request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get user profile", description = "Get user profile by user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile found",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "User profile not found",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/profiles/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> getProfile(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId,
            Authentication authentication) {
        
        log.debug("Getting user profile for user: {}", userId);
        
        // Users can only view their own profile (unless admin)
        String userIdFromToken = authentication.getName();
        if (!userId.toString().equals(userIdFromToken) && 
            !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return userService.getProfile(userId)
                .map(profile -> ResponseEntity.ok(profile))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get current user profile", description = "Get the profile of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile found",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "User profile not found",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/profiles/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.debug("Getting current user profile for user: {}", userId);
        
        return userService.getProfile(userId)
                .map(profile -> ResponseEntity.ok(profile))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update user profile", description = "Update user profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "User profile not found",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PutMapping("/profiles/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserProfileRequest request,
            Authentication authentication) {
        
        log.info("Updating user profile for user: {}", userId);
        
        // Users can only update their own profile (unless admin)
        String userIdFromToken = authentication.getName();
        if (!userId.toString().equals(userIdFromToken) && 
            !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        request.sanitize();
        UserProfileResponse response = userService.updateProfile(userId, request);
        
        log.info("User profile updated successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Partially update user profile", description = "Partially update user profile with only provided fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "User profile not found",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PatchMapping("/profiles/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> patchProfile(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId,
            @Valid @RequestBody PatchUserProfileRequest request,
            Authentication authentication) {
        
        log.info("Partially updating user profile for user: {}", userId);
        
        // Users can only update their own profile (unless admin)
        String userIdFromToken = authentication.getName();
        if (!userId.toString().equals(userIdFromToken) && 
            !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        request.sanitize();
        UserProfileResponse response = userService.patchProfile(userId, request);
        
        log.info("User profile partially updated successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload profile picture", description = "Upload a new profile picture for the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or request",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "User profile not found",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/profiles/{userId}/picture")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> uploadProfilePicture(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId,
            @Valid @RequestBody UploadProfilePictureRequest request,
            Authentication authentication) {
        
        log.info("Uploading profile picture for user: {}", userId);
        
        // Users can only upload their own picture (unless admin)
        String userIdFromToken = authentication.getName();
        if (!userId.toString().equals(userIdFromToken) && 
            !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        UserProfileResponse response = userService.uploadProfilePicture(userId, request);
        
        log.info("Profile picture uploaded successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete profile picture", description = "Remove the user's profile picture")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile picture deleted successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "User profile not found",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @DeleteMapping("/profiles/{userId}/picture")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> deleteProfilePicture(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId,
            Authentication authentication) {
        
        log.info("Deleting profile picture for user: {}", userId);
        
        // Users can only delete their own picture (unless admin)
        String userIdFromToken = authentication.getName();
        if (!userId.toString().equals(userIdFromToken) && 
            !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        UserProfileResponse response = userService.deleteProfilePicture(userId);
        
        log.info("Profile picture deleted successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search user profiles", description = "Search user profiles by name (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results found",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/profiles/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserProfileResponse>> searchProfiles(
            @Parameter(description = "Search term for name matching")
            @RequestParam String searchTerm,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Searching user profiles with term: {}", searchTerm);
        
        Page<UserProfileResponse> results = userService.searchProfiles(searchTerm, pageable);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Get all user profiles", description = "Get all active user profiles (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profiles found",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/profiles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserProfileResponse>> getAllProfiles(
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Getting all user profiles");
        
        Page<UserProfileResponse> profiles = userService.getAllActiveProfiles(pageable);
        return ResponseEntity.ok(profiles);
    }

    @Operation(summary = "Update verification status", description = "Update user verification status (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification status updated successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "User profile not found",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PatchMapping("/profiles/{userId}/verification")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> updateVerificationStatus(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateVerificationStatusRequest request,
            Authentication authentication) {
        
        log.info("Updating verification status for user: {} to: {}", userId, request.getVerificationLevel());
        
        UserProfileResponse response = userService.updateVerificationStatus(
                userId, 
                request.getIsVerified(), 
                request.getVerificationLevel(),
                authentication.getName());
        
        log.info("Verification status updated successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deactivate user profile", description = "Deactivate user profile (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User profile deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "User profile not found",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @DeleteMapping("/profiles/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateProfile(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId,
            Authentication authentication) {
        
        log.info("Deactivating user profile for user: {}", userId);
        
        userService.deactivateProfile(userId, authentication.getName());
        
        log.info("User profile deactivated successfully for user: {}", userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get profile statistics", description = "Get user profile statistics (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileStatistics.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/profiles/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileStatistics> getProfileStatistics() {
        log.debug("Getting user profile statistics");
        
        UserProfileStatistics statistics = userService.getProfileStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Export user data", description = "Export all user data for GDPR compliance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User data exported successfully",
                    content = @Content(schema = @Schema(implementation = UserDataExportResponse.class))),
            @ApiResponse(responseCode = "404", description = "User profile not found",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/profiles/{userId}/export")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserDataExportResponse> exportUserData(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId,
            Authentication authentication) {
        
        log.info("Exporting user data for user: {}", userId);
        
        // Users can only export their own data (unless admin)
        String userIdFromToken = authentication.getName();
        if (!userId.toString().equals(userIdFromToken) && 
            !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        UserDataExportResponse response = userService.exportUserData(userId);
        
        log.info("User data exported successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }
}
