package com.doordash.delivery_service.domain.dtos.delivery;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for creating a new delivery.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeliveryRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull(message = "Restaurant ID is required")
    private UUID restaurantId;

    private UUID deliveryZoneId;

    @NotNull(message = "Delivery type is required")
    private String deliveryType;

    @NotNull(message = "Priority is required")
    private String priority;

    // Pickup Address
    @NotNull(message = "Pickup address is required")
    private AddressInfo pickupAddress;

    // Delivery Address
    @NotNull(message = "Delivery address is required")
    private AddressInfo deliveryAddress;

    // Timing
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestedDeliveryTime;

    // Special requirements
    private Map<String, Object> specialRequirements;

    @Size(max = 1000, message = "Delivery instructions must not exceed 1000 characters")
    private String deliveryInstructions;

    @Size(max = 1000, message = "Pickup instructions must not exceed 1000 characters")
    private String pickupInstructions;

    // Contact Information
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    private String customerName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Customer phone must be valid")
    private String customerPhone;

    @Size(max = 100, message = "Restaurant name must not exceed 100 characters")
    private String restaurantName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Restaurant phone must be valid")
    private String restaurantPhone;

    // Order Details
    @DecimalMin(value = "0.0", message = "Order value must be positive")
    private BigDecimal orderValue;

    @DecimalMin(value = "0.0", message = "Delivery fee must be positive")
    private BigDecimal deliveryFee;

    @DecimalMin(value = "0.0", message = "Tip must be positive")
    private BigDecimal tip;

    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressInfo {
        @NotBlank(message = "Address line 1 is required")
        @Size(max = 200, message = "Address line 1 must not exceed 200 characters")
        private String addressLine1;

        @Size(max = 200, message = "Address line 2 must not exceed 200 characters")
        private String addressLine2;

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must not exceed 100 characters")
        private String city;

        @NotBlank(message = "State is required")
        @Size(max = 100, message = "State must not exceed 100 characters")
        private String state;

        @NotBlank(message = "Postal code is required")
        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        private String postalCode;

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        private Double latitude;

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        private Double longitude;

        @Size(max = 1000, message = "Instructions must not exceed 1000 characters")
        private String instructions;
    }
}
