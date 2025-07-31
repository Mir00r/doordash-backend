package com.doordash.order_service.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItems {

    @Column(name = "items", columnDefinition = "jsonb")
    private List<OrderItem> items = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private UUID menuItemId;
        private String name;
        private int quantity;
        private BigDecimal price;
    }

//    public double calculateTotal() {
//        return items.stream()
//                .mapToDouble(item -> item.getPrice() * item.getQuantity())
//                .sum();
//    }
}
