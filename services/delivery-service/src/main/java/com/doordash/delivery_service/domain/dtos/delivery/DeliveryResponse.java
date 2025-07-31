package com.doordash.delivery_service.domain.dtos.delivery;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for Delivery information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {

    private UUID id;
    private UUID orderId;
    private UUID customerId;
    private UUID restaurantId;
    private UUID driverId;
    private UUID deliveryZoneId;
    private String status;
    private String priority;
    private String deliveryType;

    // Addresses
    private AddressInfo pickupAddress;
    private AddressInfo deliveryAddress;

    // Timing
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestedDeliveryTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime estimatedPickupTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime actualPickupTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime actualDeliveryTime;

    // Pricing
    private BigDecimal deliveryFee;
    private BigDecimal driverPayout;
    private BigDecimal tip;

    // Progress and Tracking
    private Integer progressPercentage;
    private TrackingInfo trackingInfo;

    // Ratings and Feedback
    private Integer customerRating;
    private String customerFeedback;
    private Integer driverRating;
    private String driverFeedback;

    // Special requirements
    private Map<String, Object> specialRequirements;
    private String deliveryInstructions;
    private String pickupInstructions;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressInfo {
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private Double latitude;
        private Double longitude;
        private String instructions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackingInfo {
        private String currentStatus;
        private String customerFriendlyStatus;
        private LocationInfo currentLocation;
        private BigDecimal distanceToPickup;
        private BigDecimal distanceToDelivery;
        private BigDecimal distanceTraveled;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime lastUpdateTime;

        private Boolean isAtRestaurant;
        private Boolean isEnRouteToCustomer;
        private Boolean isAtDeliveryLocation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationInfo {
        private Double latitude;
        private Double longitude;
        private String address;
        private BigDecimal accuracy;
        private BigDecimal bearing;
        private BigDecimal speed;
    }
}
