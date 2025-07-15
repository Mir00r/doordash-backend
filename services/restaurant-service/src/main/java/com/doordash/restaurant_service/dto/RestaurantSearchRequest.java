package com.doordash.restaurant_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSearchRequest {

    private String name;
    private String cuisine;
    private String city;
    private String zipCode;
    private Boolean isOpen;
}