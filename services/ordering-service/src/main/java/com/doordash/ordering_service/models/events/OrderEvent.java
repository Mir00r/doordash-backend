package com.doordash.ordering_service.models.events;

import com.doordash.ordering_service.enums.OrderStatus;
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
    private OrderStatus status;
    private Double totalAmount;
    private List<OrderItem> items;
    private String eventType; // "ORDER_PLACED", "ORDER_CANCELLED", etc.
    private Instant eventTime;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private UUID menuItemId;
        private String name;
        private Integer quantity;
        private Double price;
    }
}