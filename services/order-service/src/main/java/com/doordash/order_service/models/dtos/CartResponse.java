package com.doordash.order_service.models.dtos;

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
    private BigDecimal totalAmount;
    private List<CartItemResponse> items;
}