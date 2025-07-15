package com.doordash.restaurant_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String imageUrl;
    private Boolean available;
    private Integer preparationTime;
    private Boolean vegetarian;
    private Boolean vegan;
    private Boolean glutenFree;
    private Boolean spicy;
    private Long restaurantId;
    private String restaurantName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}