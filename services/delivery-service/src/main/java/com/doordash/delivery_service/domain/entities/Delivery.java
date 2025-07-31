package com.doordash.delivery_service.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

/**
 * Delivery entity representing individual delivery requests and tracking.
 * 
 * This entity stores comprehensive delivery information including addresses,
 * timing, pricing, and tracking data for the complete delivery lifecycle.
 * 
 * Features:
 * - Complete delivery lifecycle tracking
 * - Geospatial address information with PostGIS
 * - Pricing and driver payout calculations
 * - ETA predictions and route optimization
 * - Customer and driver feedback integration
 * - Special requirements and instructions
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@Entity
@Table(name = "deliveries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId; // Reference to order-service order

    @Column(name = "customer_id", nullable = false)
    private UUID customerId; // Reference to user-service customer

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId; // Reference to restaurant-service restaurant

    @Column(name = "driver_id")
    private UUID driverId; // Assigned driver (nullable until assigned)

    @Column(name = "delivery_zone_id")
    private UUID deliveryZoneId;

    // Pickup address information
    @Column(name = "pickup_address_line1", nullable = false)
    private String pickupAddressLine1;

    @Column(name = "pickup_address_line2")
    private String pickupAddressLine2;

    @Column(name = "pickup_city", nullable = false, length = 100)
    private String pickupCity;

    @Column(name = "pickup_state", nullable = false, length = 100)
    private String pickupState;

    @Column(name = "pickup_postal_code", nullable = false, length = 20)
    private String pickupPostalCode;

    @Column(name = "pickup_location", columnDefinition = "geometry(Point,4326)")
    private Point pickupLocation;

    @Column(name = "pickup_instructions", columnDefinition = "TEXT")
    private String pickupInstructions;

    // Delivery address information
    @Column(name = "delivery_address_line1", nullable = false)
    private String deliveryAddressLine1;

    @Column(name = "delivery_address_line2")
    private String deliveryAddressLine2;

    @Column(name = "delivery_city", nullable = false, length = 100)
    private String deliveryCity;

    @Column(name = "delivery_state", nullable = false, length = 100)
    private String deliveryState;

    @Column(name = "delivery_postal_code", nullable = false, length = 20)
    private String deliveryPostalCode;

    @Column(name = "delivery_location", columnDefinition = "geometry(Point,4326)")
    private Point deliveryLocation;

    @Column(name = "delivery_instructions", columnDefinition = "TEXT")
    private String deliveryInstructions;

    // Status and timing
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", length = 30)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "priority", length = 20)
    private Priority priority = Priority.NORMAL;

    @Column(name = "estimated_pickup_time")
    private LocalDateTime estimatedPickupTime;

    @Column(name = "actual_pickup_time")
    private LocalDateTime actualPickupTime;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    // Pricing information
    @Builder.Default
    @Column(name = "base_fee", precision = 5, scale = 2)
    private BigDecimal baseFee = BigDecimal.valueOf(2.99);

    @Column(name = "delivery_fee", precision = 5, scale = 2)
    private BigDecimal deliveryFee;

    @Builder.Default
    @Column(name = "surge_multiplier", precision = 3, scale = 2)
    private BigDecimal surgeMultiplier = BigDecimal.ONE;

    @Builder.Default
    @Column(name = "tip_amount", precision = 5, scale = 2)
    private BigDecimal tipAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 8, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "driver_payout", precision = 8, scale = 2)
    private BigDecimal driverPayout;

    // Distance and route information
    @Column(name = "total_distance_miles", precision = 6, scale = 2)
    private BigDecimal totalDistanceMiles;

    @Column(name = "pickup_distance_miles", precision = 6, scale = 2)
    private BigDecimal pickupDistanceMiles;

    @Column(name = "delivery_distance_miles", precision = 6, scale = 2)
    private BigDecimal deliveryDistanceMiles;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Column(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;

    // Metadata and feedback
    @Type(JsonType.class)
    @Column(name = "special_requirements", columnDefinition = "jsonb")
    private Map<String, Object> specialRequirements;

    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes;

    @Column(name = "customer_rating")
    private Integer customerRating;

    @Column(name = "customer_feedback", columnDefinition = "TEXT")
    private String customerFeedback;

    @Column(name = "driver_notes", columnDefinition = "TEXT")
    private String driverNotes;

    // Audit fields
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
     * Enumeration for delivery status.
     */
    public enum DeliveryStatus {
        PENDING("Delivery request pending"),
        ASSIGNED("Assigned to driver"),
        PICKED_UP("Order picked up from restaurant"),
        IN_TRANSIT("Order in transit to customer"),
        DELIVERED("Order delivered successfully"),
        CANCELLED("Delivery cancelled"),
        FAILED("Delivery failed");

        private final String description;

        DeliveryStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean isCompleted() {
            return this == DELIVERED || this == CANCELLED || this == FAILED;
        }

        public boolean isActive() {
            return this == ASSIGNED || this == PICKED_UP || this == IN_TRANSIT;
        }
    }

    /**
     * Enumeration for delivery priority.
     */
    public enum Priority {
        LOW("Low priority"),
        NORMAL("Normal priority"),
        HIGH("High priority"),
        URGENT("Urgent delivery");

        private final String description;

        Priority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Get the complete pickup address as a formatted string.
     * 
     * @return Formatted pickup address
     */
    public String getFormattedPickupAddress() {
        StringBuilder address = new StringBuilder();
        address.append(pickupAddressLine1);
        
        if (pickupAddressLine2 != null && !pickupAddressLine2.trim().isEmpty()) {
            address.append(", ").append(pickupAddressLine2);
        }
        
        address.append(", ").append(pickupCity)
               .append(", ").append(pickupState)
               .append(" ").append(pickupPostalCode);
        
        return address.toString();
    }

    /**
     * Get the complete delivery address as a formatted string.
     * 
     * @return Formatted delivery address
     */
    public String getFormattedDeliveryAddress() {
        StringBuilder address = new StringBuilder();
        address.append(deliveryAddressLine1);
        
        if (deliveryAddressLine2 != null && !deliveryAddressLine2.trim().isEmpty()) {
            address.append(", ").append(deliveryAddressLine2);
        }
        
        address.append(", ").append(deliveryCity)
               .append(", ").append(deliveryState)
               .append(" ").append(deliveryPostalCode);
        
        return address.toString();
    }

    /**
     * Check if the delivery is overdue.
     * 
     * @return true if delivery is past estimated delivery time
     */
    public boolean isOverdue() {
        return estimatedDeliveryTime != null && 
               LocalDateTime.now().isAfter(estimatedDeliveryTime) &&
               !status.isCompleted();
    }

    /**
     * Calculate minutes until estimated delivery time.
     * 
     * @return minutes until delivery, or null if no estimate
     */
    public Long getMinutesUntilDelivery() {
        if (estimatedDeliveryTime == null) {
            return null;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(estimatedDeliveryTime)) {
            return 0L; // Overdue
        }
        
        return ChronoUnit.MINUTES.between(now, estimatedDeliveryTime);
    }

    /**
     * Calculate actual delivery duration in minutes.
     * 
     * @return duration in minutes, or null if not completed
     */
    public Long getActualDeliveryDurationMinutes() {
        if (createdAt == null || actualDeliveryTime == null) {
            return null;
        }
        
        return ChronoUnit.MINUTES.between(createdAt, actualDeliveryTime);
    }

    /**
     * Check if delivery has both pickup and delivery locations.
     * 
     * @return true if both locations are set
     */
    public boolean hasCompleteLocationData() {
        return pickupLocation != null && deliveryLocation != null;
    }

    /**
     * Check if delivery is high value (total amount > $50).
     * 
     * @return true if high value delivery
     */
    public boolean isHighValue() {
        return totalAmount != null && totalAmount.compareTo(BigDecimal.valueOf(50)) > 0;
    }

    /**
     * Check if delivery is eligible for batching with other deliveries.
     * 
     * @return true if eligible for batching
     */
    public boolean isEligibleForBatching() {
        return status == DeliveryStatus.PENDING || status == DeliveryStatus.ASSIGNED;
    }

    /**
     * Calculate driver payout based on delivery fee and tip.
     * 
     * @param driverCommissionRate Driver commission rate (e.g., 0.8 for 80%)
     * @return Calculated driver payout
     */
    public BigDecimal calculateDriverPayout(BigDecimal driverCommissionRate) {
        if (deliveryFee == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal basePayout = deliveryFee.multiply(driverCommissionRate);
        BigDecimal tip = tipAmount != null ? tipAmount : BigDecimal.ZERO;
        
        return basePayout.add(tip);
    }

    /**
     * Update delivery status and set appropriate timestamps.
     * 
     * @param newStatus New delivery status
     */
    public void updateStatus(DeliveryStatus newStatus) {
        this.status = newStatus;
        
        switch (newStatus) {
            case PICKED_UP:
                if (actualPickupTime == null) {
                    actualPickupTime = LocalDateTime.now();
                }
                break;
            case DELIVERED:
                if (actualDeliveryTime == null) {
                    actualDeliveryTime = LocalDateTime.now();
                }
                if (actualDurationMinutes == null && createdAt != null) {
                    actualDurationMinutes = (int) ChronoUnit.MINUTES.between(createdAt, actualDeliveryTime);
                }
                break;
            default:
                // No specific timestamp updates for other statuses
                break;
        }
    }

    /**
     * Check if the delivery requires special handling.
     * 
     * @return true if special requirements exist
     */
    public boolean requiresSpecialHandling() {
        return specialRequirements != null && !specialRequirements.isEmpty();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        
        // Calculate total amount if not set
        if (totalAmount == null && deliveryFee != null) {
            BigDecimal tip = tipAmount != null ? tipAmount : BigDecimal.ZERO;
            totalAmount = deliveryFee.add(tip);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
