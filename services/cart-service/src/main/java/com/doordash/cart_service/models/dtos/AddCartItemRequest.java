package com.doordash.cart_service.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCartItemRequest {
    
    @NotNull(message = "Restaurant ID is required")
    private UUID restaurantId;
    
    @NotNull(message = "Menu item ID is required")
    private UUID menuItemId;
    
    private String restaurantName;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}