package com.doordash.user_service.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UserProfile entity representing user profile information.
 * 
 * This entity stores comprehensive user profile data including personal information,
 * verification status, and metadata for the DoorDash platform.
 * 
 * Features:
 * - Complete user profile management
 * - Verification levels and status tracking
 * - Audit trail with creation and modification tracking
 * - Soft delete support with is_active flag
 * - Integration with Auth Service through user_id reference
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@Entity
@Table(name = "user_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId; // Reference to auth-service user

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "profile_picture_url", columnDefinition = "TEXT")
    private String profilePictureUrl;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "verification_level", length = 20)
    private VerificationLevel verificationLevel = VerificationLevel.BASIC;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * Enumeration for user verification levels.
     */
    public enum VerificationLevel {
        BASIC,           // Basic registration completed
        PHONE_VERIFIED,  // Phone number verified
        ID_VERIFIED,     // Government ID verified
        FULL            // Fully verified user
    }

    /**
     * Get the full name of the user.
     * 
     * @return The full name combining first and last name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if the user profile is fully completed.
     * 
     * @return true if all essential fields are filled
     */
    public boolean isProfileComplete() {
        return firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty() &&
               phoneNumber != null && !phoneNumber.trim().isEmpty() &&
               isActive;
    }

    /**
     * Check if the user is eligible for advanced features.
     * 
     * @return true if user is verified and profile is complete
     */
    public boolean isEligibleForAdvancedFeatures() {
        return isVerified && isProfileComplete() && 
               (verificationLevel == VerificationLevel.ID_VERIFIED || 
                verificationLevel == VerificationLevel.FULL);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = getFullName();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = getFullName();
        }
    }
}
