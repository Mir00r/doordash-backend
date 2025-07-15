package com.doordash.ordering_service.models.dtos;

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
    private UUID dasherId;
    private Instant orderTime;
    private String status;
    private Double totalAmount;
    private List<OrderItemResponse> items;
    private String paymentStatus;
    private String restaurantName;
    private String estimatedDeliveryTime;
    private DeliveryAddressResponse deliveryAddress;
}