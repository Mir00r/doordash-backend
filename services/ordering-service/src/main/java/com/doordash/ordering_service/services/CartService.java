package com.doordash.ordering_service.services;

import com.doordash.ordering_service.models.dtos.CartRequest;
import com.doordash.ordering_service.models.dtos.CartResponse;
import com.doordash.ordering_service.models.entities.Cart;

import java.util.UUID;

public interface CartService {
    /**
     * Add an item to the customer's cart
     * @param cartRequest the request containing item details
     * @return the updated cart response
     */
    CartResponse addToCart(CartRequest cartRequest);
    
    /**
     * Update the quantity of an item in the cart
     * @param customerId the customer ID
     * @param menuItemId the menu item ID
     * @param quantity the new quantity
     * @return the updated cart response
     */
    CartResponse updateCartItem(UUID customerId, UUID menuItemId, int quantity);
    
    /**
     * Remove an item from the cart
     * @param customerId the customer ID
     * @param menuItemId the menu item ID
     * @return the updated cart response
     */
    CartResponse removeFromCart(UUID customerId, UUID menuItemId);
    
    /**
     * Get the customer's current cart
     * @param customerId the customer ID
     * @return the cart response
     */
    CartResponse getCart(UUID customerId);
    
    /**
     * Clear the customer's cart
     * @param customerId the customer ID
     */
    void clearCart(UUID customerId);
    
    /**
     * Get the cart entity by customer ID
     * @param customerId the customer ID
     * @return the cart entity
     */
    Cart getCartEntity(UUID customerId);
}