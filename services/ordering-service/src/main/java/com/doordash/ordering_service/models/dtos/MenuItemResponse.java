package com.doordash.ordering_service.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private UUID menuItemId;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private String category;
    private Boolean isPopular;
    private Boolean isVegetarian;
    private Boolean isVegan;
    private Boolean isGlutenFree;
}