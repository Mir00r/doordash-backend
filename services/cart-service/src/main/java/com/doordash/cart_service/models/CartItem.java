package com.doordash.cart_service.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {
    private UUID menuItemId;
    private String name;
    private String description;
    private BigDecimal price;
    private int quantity;
    private UUID restaurantId;
    private String restaurantName;
    private String imageUrl;
}