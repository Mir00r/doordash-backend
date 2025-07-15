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
public class CartResponse {
    private UUID cartId;
    private UUID customerId;
    private UUID restaurantId;
    private String restaurantName;
    private List<CartItemResponse> items;
    private Double totalAmount;
}