package com.doordash.ordering_service.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private UUID restaurantId;
    private String name;
    private String description;
    private String imageUrl;
    private String cuisineType;
    private Double rating;
    private Integer reviewCount;
    private String priceRange;
    private Integer estimatedDeliveryTime;
    private Double deliveryFee;
    private AddressResponse address;
    private Double distance;
    private List<MenuItemResponse> popularItems;
    private Boolean isOpen;
}