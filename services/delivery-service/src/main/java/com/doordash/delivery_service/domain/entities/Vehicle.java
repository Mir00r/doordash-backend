package com.doordash.delivery_service.domain.entities;

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
 * Vehicle entity representing delivery vehicles used by drivers.
 * 
 * This entity stores vehicle information, registration details,
 * insurance, and maintenance records for compliance and tracking.
 * 
 * Features:
 * - Vehicle registration and documentation
 * - Insurance and inspection tracking
 * - Maintenance and service records
 * - Driver assignment and availability
 * - Capacity and delivery type restrictions
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@Entity
@Table(name = "vehicles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @Column(name = "make", nullable = false, length = 50)
    private String make;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "color", length = 30)
    private String color;

    @Column(name = "license_plate", nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Column(name = "vin", unique = true, length = 17)
    private String vin;

    // Insurance Information
    @Column(name = "insurance_provider", length = 100)
    private String insuranceProvider;

    @Column(name = "insurance_policy_number", length = 50)
    private String insurancePolicyNumber;

    @Column(name = "insurance_expiry_date")
    private LocalDate insuranceExpiryDate;

    // Registration Information
    @Column(name = "registration_number", length = 50)
    private String registrationNumber;

    @Column(name = "registration_expiry_date")
    private LocalDate registrationExpiryDate;

    // Inspection Information
    @Column(name = "last_inspection_date")
    private LocalDate lastInspectionDate;

    @Column(name = "next_inspection_due")
    private LocalDate nextInspectionDue;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_status")
    private InspectionStatus inspectionStatus;

    // Vehicle Specifications
    @Column(name = "capacity_volume", precision = 8, scale = 2)
    private Double capacityVolume; // in cubic feet

    @Column(name = "capacity_weight", precision = 8, scale = 2)
    private Double capacityWeight; // in pounds

    @Column(name = "fuel_type", length = 20)
    private String fuelType;

    @Column(name = "mileage", precision = 8, scale = 2)
    private Double mileage;

    // Status and Availability
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VehicleStatus status;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    // Maintenance
    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @Column(name = "next_maintenance_due")
    private LocalDate nextMaintenanceDue;

    @Column(name = "maintenance_notes", columnDefinition = "TEXT")
    private String maintenanceNotes;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    /**
     * Vehicle types supported by the platform
     */
    public enum VehicleType {
        BICYCLE,
        MOTORCYCLE,
        SCOOTER,
        CAR,
        VAN,
        TRUCK,
        WALKING
    }

    /**
     * Vehicle status enumeration
     */
    public enum VehicleStatus {
        ACTIVE,
        INACTIVE,
        MAINTENANCE,
        INSPECTION_REQUIRED,
        INSURANCE_EXPIRED,
        REGISTRATION_EXPIRED,
        SUSPENDED,
        RETIRED
    }

    /**
     * Inspection status enumeration
     */
    public enum InspectionStatus {
        PASSED,
        FAILED,
        PENDING,
        OVERDUE,
        NOT_REQUIRED
    }

    // Business logic methods

    /**
     * Check if vehicle requires immediate attention
     */
    public boolean requiresAttention() {
        LocalDate today = LocalDate.now();
        
        return status == VehicleStatus.MAINTENANCE ||
               status == VehicleStatus.INSPECTION_REQUIRED ||
               status == VehicleStatus.INSURANCE_EXPIRED ||
               status == VehicleStatus.REGISTRATION_EXPIRED ||
               (insuranceExpiryDate != null && insuranceExpiryDate.isBefore(today)) ||
               (registrationExpiryDate != null && registrationExpiryDate.isBefore(today)) ||
               (nextInspectionDue != null && nextInspectionDue.isBefore(today));
    }

    /**
     * Check if vehicle is available for deliveries
     */
    public boolean isAvailableForDelivery() {
        return isActive && 
               isVerified && 
               status == VehicleStatus.ACTIVE && 
               !requiresAttention();
    }

    /**
     * Get days until next required action
     */
    public long getDaysUntilNextAction() {
        LocalDate today = LocalDate.now();
        LocalDate nextActionDate = null;

        if (insuranceExpiryDate != null) {
            nextActionDate = earliestDate(nextActionDate, insuranceExpiryDate);
        }
        if (registrationExpiryDate != null) {
            nextActionDate = earliestDate(nextActionDate, registrationExpiryDate);
        }
        if (nextInspectionDue != null) {
            nextActionDate = earliestDate(nextActionDate, nextInspectionDue);
        }
        if (nextMaintenanceDue != null) {
            nextActionDate = earliestDate(nextActionDate, nextMaintenanceDue);
        }

        return nextActionDate != null ? today.until(nextActionDate).getDays() : Long.MAX_VALUE;
    }

    private LocalDate earliestDate(LocalDate current, LocalDate candidate) {
        if (current == null) return candidate;
        return candidate.isBefore(current) ? candidate : current;
    }

    /**
     * Update vehicle status based on current conditions
     */
    public void updateStatusBasedOnConditions() {
        LocalDate today = LocalDate.now();

        if (insuranceExpiryDate != null && insuranceExpiryDate.isBefore(today)) {
            this.status = VehicleStatus.INSURANCE_EXPIRED;
        } else if (registrationExpiryDate != null && registrationExpiryDate.isBefore(today)) {
            this.status = VehicleStatus.REGISTRATION_EXPIRED;
        } else if (nextInspectionDue != null && nextInspectionDue.isBefore(today)) {
            this.status = VehicleStatus.INSPECTION_REQUIRED;
        } else if (nextMaintenanceDue != null && nextMaintenanceDue.isBefore(today)) {
            this.status = VehicleStatus.MAINTENANCE;
        } else if (this.status != VehicleStatus.SUSPENDED && this.status != VehicleStatus.RETIRED) {
            this.status = VehicleStatus.ACTIVE;
        }
    }

    /**
     * Calculate vehicle age in years
     */
    public int getVehicleAge() {
        return LocalDate.now().getYear() - this.year;
    }

    /**
     * Check if vehicle is suitable for a specific delivery type
     */
    public boolean isSuitableForDelivery(String deliveryType, Double weight, Double volume) {
        if (!isAvailableForDelivery()) {
            return false;
        }

        // Check weight capacity
        if (weight != null && capacityWeight != null && weight > capacityWeight) {
            return false;
        }

        // Check volume capacity
        if (volume != null && capacityVolume != null && volume > capacityVolume) {
            return false;
        }

        // Check vehicle type compatibility with delivery type
        return switch (deliveryType != null ? deliveryType.toUpperCase() : "STANDARD") {
            case "ALCOHOL", "PHARMACY" -> vehicleType != VehicleType.BICYCLE && vehicleType != VehicleType.WALKING;
            case "LARGE_ORDER" -> vehicleType == VehicleType.CAR || vehicleType == VehicleType.VAN || vehicleType == VehicleType.TRUCK;
            case "EXPRESS" -> vehicleType == VehicleType.MOTORCYCLE || vehicleType == VehicleType.SCOOTER || vehicleType == VehicleType.BICYCLE;
            default -> true; // Standard delivery - all vehicle types allowed
        };
    }
}
