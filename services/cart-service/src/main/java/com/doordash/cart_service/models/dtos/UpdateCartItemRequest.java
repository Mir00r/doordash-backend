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
public class UpdateCartItemRequest {
    
    @NotNull(message = "Menu item ID is required")
    private UUID menuItemId;
    
    @Min(value = 0, message = "Quantity must be at least 0")
    private int quantity;
}