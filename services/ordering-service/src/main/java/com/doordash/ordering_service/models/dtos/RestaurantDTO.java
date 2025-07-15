package com.doordash.ordering_service.models.dtos;

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
public class RestaurantDTO {
    private Long id;
    private String name;
    private String description;
    private String cuisine;
    private String imageUrl;
    private String address;
    private Double latitude;
    private Double longitude;
    private BigDecimal deliveryFee;
    private Integer estimatedDeliveryTime;
    private Double rating;
    private Integer reviewCount;
    private Boolean isOpen;
    private List<MenuItemDTO> menuItems;
}