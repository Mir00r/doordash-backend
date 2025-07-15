package com.doordash.cart_service.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("carts")
public class Cart implements Serializable {

    @Id
    private UUID id;
    
    @Indexed
    private UUID customerId;
    
    private UUID restaurantId;
    private String restaurantName;
    private List<CartItem> items;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant updatedAt;
    
    public void addItem(CartItem newItem) {
        if (items == null) {
            items = new ArrayList<>();
        }
        
        // Check if restaurant is different
        if (restaurantId != null && !restaurantId.equals(newItem.getRestaurantId())) {
            // Clear cart if adding item from a different restaurant
            items.clear();
            restaurantId = newItem.getRestaurantId();
            restaurantName = newItem.getRestaurantName();
        } else if (restaurantId == null) {
            // First item in cart
            restaurantId = newItem.getRestaurantId();
            restaurantName = newItem.getRestaurantName();
        }
        
        // Check if item already exists in cart
        boolean itemExists = false;
        for (CartItem item : items) {
            if (item.getMenuItemId().equals(newItem.getMenuItemId())) {
                // Update quantity
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                itemExists = true;
                break;
            }
        }
        
        // Add new item if it doesn't exist
        if (!itemExists) {
            items.add(newItem);
        }
        
        // Recalculate total amount
        calculateTotalAmount();
        
        // Update timestamps
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }
    
    public void updateItemQuantity(UUID menuItemId, int quantity) {
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        
        boolean itemFound = false;
        if (quantity <= 0) {
            // Remove item if quantity is 0 or negative
            items.removeIf(item -> item.getMenuItemId().equals(menuItemId));
            itemFound = true;
        } else {
            // Update quantity
            for (CartItem item : items) {
                if (item.getMenuItemId().equals(menuItemId)) {
                    item.setQuantity(quantity);
                    itemFound = true;
                    break;
                }
            }
        }
        
        if (!itemFound) {
            throw new IllegalArgumentException("Item not found in cart");
        }
        
        // Recalculate total amount
        calculateTotalAmount();
        
        // Update timestamp
        updatedAt = Instant.now();
    }
    
    public void removeItem(UUID menuItemId) {
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        
        boolean itemRemoved = items.removeIf(item -> item.getMenuItemId().equals(menuItemId));
        
        if (!itemRemoved) {
            throw new IllegalArgumentException("Item not found in cart");
        }
        
        // Recalculate total amount
        calculateTotalAmount();
        
        // Update timestamp
        updatedAt = Instant.now();
    }
    
    public void clear() {
        if (items != null) {
            items.clear();
        }
        restaurantId = null;
        restaurantName = null;
        totalAmount = BigDecimal.ZERO;
        updatedAt = Instant.now();
    }
    
    private void calculateTotalAmount() {
        if (items == null || items.isEmpty()) {
            totalAmount = BigDecimal.ZERO;
            return;
        }
        
        totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}