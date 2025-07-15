package com.doordash.order_service.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private UUID orderId;
    private UUID customerId;
    private UUID restaurantId;
    private String restaurantName;
    private UUID dasherId;
    private Instant orderTime;
    private String status;
    private Double totalAmount;
    private List<OrderItemResponse> items;
    private String paymentStatus;
}