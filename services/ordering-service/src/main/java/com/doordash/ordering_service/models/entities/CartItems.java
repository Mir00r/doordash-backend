package com.doordash.ordering_service.models.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItems {
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
    
    public void addItem(CartItem item) {
        // Check if item already exists, if so, update quantity
        for (CartItem existingItem : items) {
            if (existingItem.getMenuItemId().equals(item.getMenuItemId())) {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                return;
            }
        }
        // If item doesn't exist, add it
        items.add(item);
    }
    
    public void updateItem(UUID menuItemId, int quantity) {
        for (CartItem item : items) {
            if (item.getMenuItemId().equals(menuItemId)) {
                item.setQuantity(quantity);
                return;
            }
        }
    }
    
    public void removeItem(UUID menuItemId) {
        items.removeIf(item -> item.getMenuItemId().equals(menuItemId));
    }
    
    public double calculateTotal() {
        return items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItem {
        private UUID menuItemId;
        private String name;
        private Integer quantity;
        private Double price;
        private String imageUrl;
    }
}