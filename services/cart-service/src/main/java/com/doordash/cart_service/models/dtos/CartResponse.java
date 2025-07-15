package com.doordash.cart_service.models.dtos;

import com.doordash.cart_service.models.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private UUID cartId;
    private UUID customerId;
    private UUID restaurantId;
    private String restaurantName;
    private List<CartItem> items;
    private BigDecimal totalAmount;
}