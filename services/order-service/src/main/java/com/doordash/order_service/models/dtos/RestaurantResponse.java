package com.doordash.order_service.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private UUID id;
    private String name;
    private String description;
    private String cuisine;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String phone;
    private String email;
    private String website;
    private BigDecimal rating;
    private boolean isOpen;
    private String imageUrl;
    private BigDecimal deliveryFee;
    private int estimatedDeliveryTime;
}