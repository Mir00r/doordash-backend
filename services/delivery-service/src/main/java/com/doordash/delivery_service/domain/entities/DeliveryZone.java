package com.doordash.delivery_service.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DeliveryZone entity representing geographical zones for delivery operations.
 * 
 * This entity defines delivery boundaries, pricing rules, driver assignments,
 * and operational parameters for specific geographical areas.
 * 
 * Features:
 * - Geospatial zone boundaries with PostGIS
 * - Dynamic pricing and delivery fees
 * - Driver capacity and availability management
 * - Peak hour and surge pricing configuration
 * - Zone-specific delivery rules and restrictions
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@Entity
@Table(name = "delivery_zones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryZone {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Geographical boundary
    @Column(name = "boundary", columnDefinition = "geometry(Polygon,4326)", nullable = false)
    private Polygon boundary;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "postal_codes", columnDefinition = "TEXT")
    private String postalCodes; // Comma-separated list

    // Zone Status and Configuration
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ZoneStatus status;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "priority", nullable = false)
    private Integer priority = 1; // Lower number = higher priority

    // Delivery Configuration
    @Column(name = "max_delivery_distance_km", precision = 8, scale = 2)
    private BigDecimal maxDeliveryDistanceKm;

    @Column(name = "estimated_delivery_time_minutes")
    private Integer estimatedDeliveryTimeMinutes;

    @Column(name = "max_drivers_capacity")
    private Integer maxDriversCapacity;

    @Column(name = "current_active_drivers")
    private Integer currentActiveDrivers = 0;

    // Pricing Configuration
    @Column(name = "base_delivery_fee", precision = 10, scale = 2)
    private BigDecimal baseDeliveryFee;

    @Column(name = "per_km_rate", precision = 10, scale = 4)
    private BigDecimal perKmRate;

    @Column(name = "surge_multiplier", precision = 4, scale = 2)
    private BigDecimal surgeMultiplier = BigDecimal.ONE;

    @Column(name = "peak_hour_multiplier", precision = 4, scale = 2)
    private BigDecimal peakHourMultiplier = BigDecimal.ONE;

    @Column(name = "minimum_order_value", precision = 10, scale = 2)
    private BigDecimal minimumOrderValue;

    @Column(name = "small_order_fee", precision = 10, scale = 2)
    private BigDecimal smallOrderFee;

    // Operational Configuration
    @Type(JsonType.class)
    @Column(name = "operating_hours", columnDefinition = "jsonb")
    private Map<String, String> operatingHours; // Day -> "start_time-end_time"

    @Type(JsonType.class)
    @Column(name = "peak_hours", columnDefinition = "jsonb")
    private Map<String, String> peakHours; // Day -> "start_time-end_time"

    @Type(JsonType.class)
    @Column(name = "delivery_restrictions", columnDefinition = "jsonb")
    private Map<String, Object> deliveryRestrictions;

    @Type(JsonType.class)
    @Column(name = "special_requirements", columnDefinition = "jsonb")
    private Map<String, Object> specialRequirements;

    // Performance Metrics
    @Column(name = "average_delivery_time_minutes", precision = 8, scale = 2)
    private BigDecimal averageDeliveryTimeMinutes;

    @Column(name = "delivery_success_rate", precision = 5, scale = 4)
    private BigDecimal deliverySuccessRate;

    @Column(name = "customer_satisfaction_rating", precision = 3, scale = 2)
    private BigDecimal customerSatisfactionRating;

    @Column(name = "total_deliveries_completed")
    private Long totalDeliveriesCompleted = 0L;

    @Column(name = "total_deliveries_failed")
    private Long totalDeliveriesFailed = 0L;

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
     * Zone status enumeration
     */
    public enum ZoneStatus {
        ACTIVE,
        INACTIVE,
        TEMPORARILY_CLOSED,
        MAINTENANCE,
        CAPACITY_FULL,
        WEATHER_RESTRICTED,
        EMERGENCY_SHUTDOWN
    }

    // Business logic methods

    /**
     * Check if zone is currently operational
     */
    public boolean isOperational() {
        return isActive && 
               status == ZoneStatus.ACTIVE && 
               hasCapacityForMoreDrivers();
    }

    /**
     * Check if zone has capacity for more drivers
     */
    public boolean hasCapacityForMoreDrivers() {
        return maxDriversCapacity == null || 
               currentActiveDrivers < maxDriversCapacity;
    }

    /**
     * Calculate current capacity utilization percentage
     */
    public BigDecimal getCapacityUtilization() {
        if (maxDriversCapacity == null || maxDriversCapacity == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(currentActiveDrivers)
                .divide(BigDecimal.valueOf(maxDriversCapacity), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Calculate delivery fee for a given distance and order value
     */
    public BigDecimal calculateDeliveryFee(BigDecimal distanceKm, BigDecimal orderValue) {
        BigDecimal fee = baseDeliveryFee != null ? baseDeliveryFee : BigDecimal.ZERO;
        
        // Add distance-based fee
        if (perKmRate != null && distanceKm != null) {
            fee = fee.add(perKmRate.multiply(distanceKm));
        }
        
        // Apply surge multiplier
        if (surgeMultiplier != null) {
            fee = fee.multiply(surgeMultiplier);
        }
        
        // Apply peak hour multiplier if currently in peak hours
        if (isCurrentlyPeakHour()) {
            fee = fee.multiply(peakHourMultiplier != null ? peakHourMultiplier : BigDecimal.ONE);
        }
        
        // Add small order fee if applicable
        if (orderValue != null && minimumOrderValue != null && 
            orderValue.compareTo(minimumOrderValue) < 0 && smallOrderFee != null) {
            fee = fee.add(smallOrderFee);
        }
        
        return fee;
    }

    /**
     * Check if it's currently peak hour
     */
    private boolean isCurrentlyPeakHour() {
        // Implementation would check current time against peak_hours configuration
        // This is a simplified version
        return false;
    }

    /**
     * Check if zone is currently operating
     */
    public boolean isCurrentlyOperating() {
        // Implementation would check current time against operating_hours configuration
        // This is a simplified version
        return isOperational();
    }

    /**
     * Update zone metrics
     */
    public void updateDeliveryMetrics(boolean successful, BigDecimal deliveryTimeMinutes) {
        if (successful) {
            this.totalDeliveriesCompleted++;
        } else {
            this.totalDeliveriesFailed++;
        }
        
        // Update success rate
        long totalDeliveries = this.totalDeliveriesCompleted + this.totalDeliveriesFailed;
        if (totalDeliveries > 0) {
            this.deliverySuccessRate = BigDecimal.valueOf(this.totalDeliveriesCompleted)
                    .divide(BigDecimal.valueOf(totalDeliveries), 4, BigDecimal.ROUND_HALF_UP);
        }
        
        // Update average delivery time (simplified calculation)
        if (successful && deliveryTimeMinutes != null) {
            if (this.averageDeliveryTimeMinutes == null) {
                this.averageDeliveryTimeMinutes = deliveryTimeMinutes;
            } else {
                // Weighted average calculation
                BigDecimal totalTime = this.averageDeliveryTimeMinutes
                        .multiply(BigDecimal.valueOf(this.totalDeliveriesCompleted - 1))
                        .add(deliveryTimeMinutes);
                this.averageDeliveryTimeMinutes = totalTime
                        .divide(BigDecimal.valueOf(this.totalDeliveriesCompleted), 2, BigDecimal.ROUND_HALF_UP);
            }
        }
    }

    /**
     * Adjust surge multiplier based on demand
     */
    public void adjustSurgeMultiplier(BigDecimal demandFactor) {
        if (demandFactor != null) {
            // Simple surge calculation - could be more sophisticated
            this.surgeMultiplier = BigDecimal.ONE.add(
                demandFactor.subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(0.5))
            );
            
            // Cap surge multiplier
            BigDecimal maxSurge = BigDecimal.valueOf(3.0);
            if (this.surgeMultiplier.compareTo(maxSurge) > 0) {
                this.surgeMultiplier = maxSurge;
            }
            
            // Minimum surge multiplier
            BigDecimal minSurge = BigDecimal.valueOf(0.8);
            if (this.surgeMultiplier.compareTo(minSurge) < 0) {
                this.surgeMultiplier = minSurge;
            }
        }
    }

    /**
     * Check if zone can handle additional delivery
     */
    public boolean canHandleAdditionalDelivery() {
        return isOperational() && 
               (status != ZoneStatus.CAPACITY_FULL) &&
               hasCapacityForMoreDrivers();
    }

    /**
     * Get priority score for zone assignment
     */
    public int getPriorityScore() {
        int score = priority;
        
        // Adjust based on capacity utilization
        BigDecimal utilization = getCapacityUtilization();
        if (utilization.compareTo(BigDecimal.valueOf(80)) > 0) {
            score += 10; // Lower priority if near capacity
        } else if (utilization.compareTo(BigDecimal.valueOf(50)) < 0) {
            score -= 5; // Higher priority if low utilization
        }
        
        // Adjust based on success rate
        if (deliverySuccessRate != null && deliverySuccessRate.compareTo(BigDecimal.valueOf(0.95)) > 0) {
            score -= 3; // Higher priority for high-performing zones
        }
        
        return score;
    }
}
