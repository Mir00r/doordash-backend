package com.doordash.restaurant_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemSearchRequest {

    private String name;
    private String category;
    private Boolean available;
    private Boolean vegetarian;
    private Boolean vegan;
    private Boolean glutenFree;
    private Boolean spicy;
}