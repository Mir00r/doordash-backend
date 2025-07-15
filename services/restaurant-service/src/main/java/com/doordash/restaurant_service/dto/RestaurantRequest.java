package com.doordash.restaurant_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    private String city;

    private String state;

    private String zipCode;

    private String phoneNumber;

    @Email(message = "Invalid email format")
    private String email;

    private String website;

    @NotBlank(message = "Cuisine is required")
    private String cuisine;

    @NotNull(message = "Delivery fee is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Delivery fee must be greater than or equal to 0")
    private BigDecimal deliveryFee;

    @NotNull(message = "Estimated delivery time is required")
    private Integer estimatedDeliveryTime;

    @NotNull(message = "Open status is required")
    private Boolean isOpen;

    private String imageUrl;

    @Valid
    private List<RestaurantHoursRequest> hours;
}