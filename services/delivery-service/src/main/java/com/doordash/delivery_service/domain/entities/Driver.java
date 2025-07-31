package com.doordash.delivery_service.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Driver entity representing delivery drivers in the platform.
 * 
 * This entity stores comprehensive driver information including profile data,
 * verification status, location tracking, and performance metrics.
 * 
 * Features:
 * - Driver profile and contact information
 * - Background check and verification status
 * - Real-time location tracking with PostGIS
 * - Performance metrics and ratings
 * - Earnings and payout information
 * - Availability and status management
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@Entity
@Table(name = "drivers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId; // Reference to auth-service user

    @Column(name = "driver_license_number", nullable = false, unique = true, length = 50)
    private String driverLicenseNumber;

    @Column(name = "license_expiry_date", nullable = false)
    private LocalDate licenseExpiryDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "background_check_status", length = 20)
    private BackgroundCheckStatus backgroundCheckStatus = BackgroundCheckStatus.PENDING;

    @Column(name = "background_check_date")
    private LocalDateTime backgroundCheckDate;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber; // Should be encrypted

    @Column(name = "routing_number", length = 20)
    private String routingNumber; // Should be encrypted

    @Column(name = "tax_id", length = 20)
    private String taxId; // Should be encrypted

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "driver_status", length = 20)
    private DriverStatus driverStatus = DriverStatus.INACTIVE;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "availability_status", length = 20)
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.OFFLINE;

    @Column(name = "current_location", columnDefinition = "geometry(Point,4326)")
    private Point currentLocation;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    @Builder.Default
    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;

    @Builder.Default
    @Column(name = "successful_deliveries")
    private Integer successfulDeliveries = 0;

    @Builder.Default
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_earnings", precision = 10, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

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
     * Enumeration for background check status.
     */
    public enum BackgroundCheckStatus {
        PENDING("Background check pending"),
        APPROVED("Background check approved"),
        REJECTED("Background check rejected"),
        EXPIRED("Background check expired");

        private final String description;

        BackgroundCheckStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Enumeration for driver status.
     */
    public enum DriverStatus {
        INACTIVE("Driver account inactive"),
        ACTIVE("Driver account active"),
        SUSPENDED("Driver account suspended"),
        BANNED("Driver account banned");

        private final String description;

        DriverStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Enumeration for availability status.
     */
    public enum AvailabilityStatus {
        OFFLINE("Driver is offline"),
        AVAILABLE("Driver is available for deliveries"),
        BUSY("Driver is currently on a delivery"),
        ON_BREAK("Driver is on break");

        private final String description;

        AvailabilityStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Check if the driver is eligible to receive deliveries.
     * 
     * @return true if driver can receive deliveries
     */
    public boolean isEligibleForDeliveries() {
        return isActive &&
               driverStatus == DriverStatus.ACTIVE &&
               availabilityStatus == AvailabilityStatus.AVAILABLE &&
               backgroundCheckStatus == BackgroundCheckStatus.APPROVED &&
               licenseExpiryDate.isAfter(LocalDate.now());
    }

    /**
     * Check if the driver profile is complete.
     * 
     * @return true if all required fields are filled
     */
    public boolean isProfileComplete() {
        return driverLicenseNumber != null && !driverLicenseNumber.trim().isEmpty() &&
               phoneNumber != null && !phoneNumber.trim().isEmpty() &&
               licenseExpiryDate != null &&
               emergencyContactName != null && !emergencyContactName.trim().isEmpty() &&
               emergencyContactPhone != null && !emergencyContactPhone.trim().isEmpty();
    }

    /**
     * Check if the driver needs verification.
     * 
     * @return true if driver needs verification
     */
    public boolean needsVerification() {
        return backgroundCheckStatus == BackgroundCheckStatus.PENDING ||
               backgroundCheckStatus == BackgroundCheckStatus.EXPIRED ||
               (licenseExpiryDate != null && licenseExpiryDate.isBefore(LocalDate.now().plusDays(30)));
    }

    /**
     * Calculate the driver's success rate.
     * 
     * @return success rate as a percentage (0-100)
     */
    public BigDecimal getSuccessRate() {
        if (totalDeliveries == null || totalDeliveries == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(successfulDeliveries)
                .divide(BigDecimal.valueOf(totalDeliveries), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Check if the driver is highly rated.
     * 
     * @return true if driver has high rating (>= 4.5)
     */
    public boolean isHighlyRated() {
        return averageRating != null && averageRating.compareTo(BigDecimal.valueOf(4.5)) >= 0;
    }

    /**
     * Check if the driver is new (less than 10 deliveries).
     * 
     * @return true if driver is new
     */
    public boolean isNewDriver() {
        return totalDeliveries != null && totalDeliveries < 10;
    }

    /**
     * Check if the driver is experienced (more than 100 deliveries).
     * 
     * @return true if driver is experienced
     */
    public boolean isExperiencedDriver() {
        return totalDeliveries != null && totalDeliveries > 100;
    }

    /**
     * Update the driver's location.
     * 
     * @param location New location point
     */
    public void updateLocation(Point location) {
        this.currentLocation = location;
        this.lastLocationUpdate = LocalDateTime.now();
    }

    /**
     * Update delivery statistics.
     * 
     * @param wasSuccessful Whether the delivery was successful
     * @param rating Rating for this delivery (optional)
     * @param earnings Earnings from this delivery
     */
    public void updateDeliveryStats(boolean wasSuccessful, BigDecimal rating, BigDecimal earnings) {
        if (totalDeliveries == null) {
            totalDeliveries = 0;
        }
        if (successfulDeliveries == null) {
            successfulDeliveries = 0;
        }
        if (totalEarnings == null) {
            totalEarnings = BigDecimal.ZERO;
        }

        totalDeliveries++;
        if (wasSuccessful) {
            successfulDeliveries++;
        }

        if (earnings != null) {
            totalEarnings = totalEarnings.add(earnings);
        }

        // Update average rating if rating is provided
        if (rating != null && averageRating != null) {
            // Weighted average calculation
            BigDecimal totalRating = averageRating.multiply(BigDecimal.valueOf(totalDeliveries - 1));
            totalRating = totalRating.add(rating);
            averageRating = totalRating.divide(BigDecimal.valueOf(totalDeliveries), 2, BigDecimal.ROUND_HALF_UP);
        } else if (rating != null && averageRating == null) {
            averageRating = rating;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
