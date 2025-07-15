package com.doordash.restaurant_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String phoneNumber;
    private String email;
    private String website;
    private String cuisine;
    private BigDecimal averageRating;
    private Integer totalRatings;
    private BigDecimal deliveryFee;
    private Integer estimatedDeliveryTime;
    private Boolean isOpen;
    private String imageUrl;
    private List<RestaurantHoursResponse> hours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}