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
import java.util.Map;
import java.util.UUID;

/**
 * DeliveryTracking entity for real-time delivery location and status tracking.
 * 
 * This entity stores location updates, timestamps, and tracking information
 * for active deliveries to provide real-time visibility to customers and operations.
 * 
 * Features:
 * - Real-time GPS location tracking with PostGIS
 * - Driver movement and route progress monitoring
 * - ETA calculations and updates
 * - Geofencing and milestone tracking
 * - Performance analytics and delivery insights
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@Entity
@Table(name = "delivery_tracking", 
       indexes = {
           @Index(name = "idx_delivery_tracking_delivery_id", columnList = "delivery_id"),
           @Index(name = "idx_delivery_tracking_driver_id", columnList = "driver_id"),
           @Index(name = "idx_delivery_tracking_timestamp", columnList = "tracking_timestamp"),
           @Index(name = "idx_delivery_tracking_status", columnList = "tracking_status")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Column(name = "tracking_timestamp", nullable = false)
    private LocalDateTime trackingTimestamp;

    // Location Information
    @Column(name = "current_location", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point currentLocation;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "altitude", precision = 8, scale = 2)
    private BigDecimal altitude; // in meters

    @Column(name = "accuracy", precision = 8, scale = 2)
    private BigDecimal accuracy; // GPS accuracy in meters

    @Column(name = "bearing", precision = 6, scale = 2)
    private BigDecimal bearing; // Direction in degrees (0-360)

    @Column(name = "speed", precision = 8, scale = 2)
    private BigDecimal speed; // Speed in km/h

    // Delivery Progress
    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_status", nullable = false)
    private TrackingStatus trackingStatus;

    @Column(name = "distance_to_pickup", precision = 10, scale = 2)
    private BigDecimal distanceToPickup; // in kilometers

    @Column(name = "distance_to_delivery", precision = 10, scale = 2)
    private BigDecimal distanceToDelivery; // in kilometers

    @Column(name = "distance_traveled", precision = 10, scale = 2)
    private BigDecimal distanceTraveled; // Total distance traveled in km

    @Column(name = "estimated_pickup_time")
    private LocalDateTime estimatedPickupTime;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "route_deviation", precision = 8, scale = 2)
    private BigDecimal routeDeviation; // Deviation from optimal route in km

    // Milestones and Events
    @Column(name = "is_at_restaurant", nullable = false)
    private Boolean isAtRestaurant = false;

    @Column(name = "restaurant_arrival_time")
    private LocalDateTime restaurantArrivalTime;

    @Column(name = "pickup_completed_time")
    private LocalDateTime pickupCompletedTime;

    @Column(name = "is_en_route_to_customer", nullable = false)
    private Boolean isEnRouteToCustomer = false;

    @Column(name = "is_at_delivery_location", nullable = false)
    private Boolean isAtDeliveryLocation = false;

    @Column(name = "delivery_location_arrival_time")
    private LocalDateTime deliveryLocationArrivalTime;

    // Geofencing
    @Column(name = "restaurant_geofence_entered")
    private LocalDateTime restaurantGeofenceEntered;

    @Column(name = "restaurant_geofence_exited")
    private LocalDateTime restaurantGeofenceExited;

    @Column(name = "delivery_geofence_entered")
    private LocalDateTime deliveryGeofenceEntered;

    @Column(name = "delivery_geofence_exited")
    private LocalDateTime deliveryGeofenceExited;

    // Additional Context
    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "battery_level")
    private Integer batteryLevel; // Driver's device battery level (0-100)

    @Column(name = "network_strength")
    private Integer networkStrength; // Signal strength (0-100)

    @Column(name = "app_version", length = 20)
    private String appVersion;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    // Weather and Traffic
    @Column(name = "weather_condition", length = 50)
    private String weatherCondition;

    @Column(name = "temperature", precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "traffic_condition")
    @Enumerated(EnumType.STRING)
    private TrafficCondition trafficCondition;

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
     * Tracking status enumeration
     */
    public enum TrackingStatus {
        DRIVER_ASSIGNED,
        EN_ROUTE_TO_RESTAURANT,
        ARRIVED_AT_RESTAURANT,
        WAITING_FOR_ORDER,
        ORDER_PICKED_UP,
        EN_ROUTE_TO_CUSTOMER,
        ARRIVED_AT_DELIVERY_LOCATION,
        DELIVERY_ATTEMPTED,
        DELIVERED,
        DELIVERY_FAILED,
        RETURNING_TO_RESTAURANT,
        CANCELLED
    }

    /**
     * Traffic condition enumeration
     */
    public enum TrafficCondition {
        LIGHT,
        MODERATE,
        HEAVY,
        SEVERE,
        UNKNOWN
    }

    // Business logic methods

    /**
     * Calculate time elapsed since last update
     */
    public long getMinutesSinceLastUpdate() {
        return LocalDateTime.now().until(trackingTimestamp, java.time.temporal.ChronoUnit.MINUTES);
    }

    /**
     * Check if tracking data is stale
     */
    public boolean isTrackingStale() {
        return getMinutesSinceLastUpdate() > 5; // More than 5 minutes old
    }

    /**
     * Calculate average speed based on distance and time
     */
    public BigDecimal calculateAverageSpeed(BigDecimal previousDistance, LocalDateTime previousTime) {
        if (previousDistance == null || previousTime == null || distanceTraveled == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal distanceDiff = distanceTraveled.subtract(previousDistance);
        long minutesDiff = previousTime.until(trackingTimestamp, java.time.temporal.ChronoUnit.MINUTES);

        if (minutesDiff == 0) {
            return BigDecimal.ZERO;
        }

        // Convert to km/h
        return distanceDiff.divide(BigDecimal.valueOf(minutesDiff), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(60));
    }

    /**
     * Update ETA based on current location and traffic
     */
    public void updateETA(BigDecimal baseTimeMinutes) {
        if (baseTimeMinutes == null) return;

        BigDecimal adjustedTime = baseTimeMinutes;

        // Adjust for traffic conditions
        if (trafficCondition != null) {
            BigDecimal trafficMultiplier = switch (trafficCondition) {
                case LIGHT -> BigDecimal.valueOf(0.9);
                case MODERATE -> BigDecimal.valueOf(1.1);
                case HEAVY -> BigDecimal.valueOf(1.3);
                case SEVERE -> BigDecimal.valueOf(1.6);
                default -> BigDecimal.ONE;
            };
            adjustedTime = adjustedTime.multiply(trafficMultiplier);
        }

        // Adjust for weather conditions
        if (weatherCondition != null && 
            (weatherCondition.toLowerCase().contains("rain") || 
             weatherCondition.toLowerCase().contains("snow"))) {
            adjustedTime = adjustedTime.multiply(BigDecimal.valueOf(1.2));
        }

        // Update delivery ETA
        this.estimatedDeliveryTime = trackingTimestamp.plusMinutes(adjustedTime.longValue());
    }

    /**
     * Check if driver is stationary
     */
    public boolean isDriverStationary() {
        return speed != null && speed.compareTo(BigDecimal.valueOf(2)) < 0; // Less than 2 km/h
    }

    /**
     * Check if driver is significantly off route
     */
    public boolean isSignificantlyOffRoute() {
        return routeDeviation != null && routeDeviation.compareTo(BigDecimal.valueOf(0.5)) > 0; // More than 500m deviation
    }

    /**
     * Get tracking status description for customers
     */
    public String getCustomerFriendlyStatus() {
        return switch (trackingStatus) {
            case DRIVER_ASSIGNED -> "Driver assigned to your order";
            case EN_ROUTE_TO_RESTAURANT -> "Driver is heading to the restaurant";
            case ARRIVED_AT_RESTAURANT -> "Driver has arrived at the restaurant";
            case WAITING_FOR_ORDER -> "Driver is waiting for your order to be prepared";
            case ORDER_PICKED_UP -> "Driver has picked up your order";
            case EN_ROUTE_TO_CUSTOMER -> "Driver is on the way to you";
            case ARRIVED_AT_DELIVERY_LOCATION -> "Driver has arrived at your location";
            case DELIVERY_ATTEMPTED -> "Driver is attempting delivery";
            case DELIVERED -> "Your order has been delivered";
            case DELIVERY_FAILED -> "Delivery attempt was unsuccessful";
            case RETURNING_TO_RESTAURANT -> "Driver is returning to restaurant";
            case CANCELLED -> "Delivery has been cancelled";
        };
    }

    /**
     * Calculate delivery progress percentage
     */
    public BigDecimal getDeliveryProgressPercentage() {
        return switch (trackingStatus) {
            case DRIVER_ASSIGNED -> BigDecimal.valueOf(10);
            case EN_ROUTE_TO_RESTAURANT -> BigDecimal.valueOf(25);
            case ARRIVED_AT_RESTAURANT -> BigDecimal.valueOf(40);
            case WAITING_FOR_ORDER -> BigDecimal.valueOf(50);
            case ORDER_PICKED_UP -> BigDecimal.valueOf(60);
            case EN_ROUTE_TO_CUSTOMER -> BigDecimal.valueOf(80);
            case ARRIVED_AT_DELIVERY_LOCATION -> BigDecimal.valueOf(90);
            case DELIVERY_ATTEMPTED -> BigDecimal.valueOf(95);
            case DELIVERED -> BigDecimal.valueOf(100);
            case DELIVERY_FAILED, CANCELLED -> BigDecimal.valueOf(0);
            case RETURNING_TO_RESTAURANT -> BigDecimal.valueOf(30);
        };
    }

    /**
     * Check if tracking requires immediate attention
     */
    public boolean requiresAttention() {
        return isTrackingStale() ||
               isSignificantlyOffRoute() ||
               (batteryLevel != null && batteryLevel < 20) ||
               (networkStrength != null && networkStrength < 30) ||
               trackingStatus == TrackingStatus.DELIVERY_FAILED;
    }
}
