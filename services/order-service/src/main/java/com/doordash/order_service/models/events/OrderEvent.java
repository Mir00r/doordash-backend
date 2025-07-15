package com.doordash.order_service.models.events;

import com.doordash.order_service.models.entities.Order;
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
public class OrderEvent {

    private UUID orderId;
    private UUID customerId;
    private UUID restaurantId;
    private String restaurantName;
    private Instant orderTime;
    private Order.OrderStatus status;
    private Double totalAmount;
    private List<OrderItem> items;
    private String eventType; // "ORDER_PLACED", "ORDER_CANCELLED", etc.
    private Instant timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private UUID menuItemId;
        private String name;
        private int quantity;
        private Double price;
    }
}