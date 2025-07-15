package com.doordash.cart_service.services;

import com.doordash.cart_service.clients.RestaurantClient;
import com.doordash.cart_service.models.Cart;
import com.doordash.cart_service.models.CartItem;
import com.doordash.cart_service.models.dtos.AddCartItemRequest;
import com.doordash.cart_service.models.dtos.CartResponse;
import com.doordash.cart_service.models.dtos.UpdateCartItemRequest;
import com.doordash.cart_service.repositories.CartRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final RestaurantClient restaurantClient;
    private final MeterRegistry meterRegistry;

    @Cacheable(value = "cart", key = "#customerId")
    public CartResponse getCart(UUID customerId) {
        log.info("Getting cart for customer: {}", customerId);
        meterRegistry.counter("cart.get").increment();
        
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> createNewCart(customerId));
        
        return mapToCartResponse(cart);
    }

    @CacheEvict(value = "cart", key = "#customerId")
    public CartResponse addItemToCart(UUID customerId, AddCartItemRequest request) {
        log.info("Adding item to cart for customer: {}", customerId);
        meterRegistry.counter("cart.add_item").increment();
        
        // Get or create cart
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> createNewCart(customerId));
        
        // Verify menu item exists in restaurant
        var menuItem = restaurantClient.getMenuItem(request.getRestaurantId(), request.getMenuItemId());
        
        // Create cart item
        CartItem cartItem = CartItem.builder()
                .menuItemId(menuItem.getId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .quantity(request.getQuantity())
                .restaurantId(menuItem.getRestaurantId())
                .restaurantName(request.getRestaurantName())
                .imageUrl(menuItem.getImageUrl())
                .build();
        
        // Add item to cart
        cart.addItem(cartItem);
        
        // Save cart
        Cart savedCart = cartRepository.save(cart);
        
        return mapToCartResponse(savedCart);
    }

    @CacheEvict(value = "cart", key = "#customerId")
    public CartResponse updateCartItem(UUID customerId, UpdateCartItemRequest request) {
        log.info("Updating item in cart for customer: {}", customerId);
        meterRegistry.counter("cart.update_item").increment();
        
        // Get cart
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        // Update item quantity
        cart.updateItemQuantity(request.getMenuItemId(), request.getQuantity());
        
        // Save cart
        Cart savedCart = cartRepository.save(cart);
        
        return mapToCartResponse(savedCart);
    }

    @CacheEvict(value = "cart", key = "#customerId")
    public CartResponse removeCartItem(UUID customerId, UUID menuItemId) {
        log.info("Removing item from cart for customer: {}", customerId);
        meterRegistry.counter("cart.remove_item").increment();
        
        // Get cart
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        // Remove item
        cart.removeItem(menuItemId);
        
        // Save cart
        Cart savedCart = cartRepository.save(cart);
        
        return mapToCartResponse(savedCart);
    }

    @CacheEvict(value = "cart", key = "#customerId")
    public void clearCart(UUID customerId) {
        log.info("Clearing cart for customer: {}", customerId);
        meterRegistry.counter("cart.clear").increment();
        
        // Get cart
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> createNewCart(customerId));
        
        // Clear cart
        cart.clear();
        
        // Save cart
        cartRepository.save(cart);
    }

    private Cart createNewCart(UUID customerId) {
        return Cart.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private CartResponse mapToCartResponse(Cart cart) {
        return CartResponse.builder()
                .cartId(cart.getId())
                .customerId(cart.getCustomerId())
                .restaurantId(cart.getRestaurantId())
                .restaurantName(cart.getRestaurantName())
                .items(cart.getItems())
                .totalAmount(cart.getTotalAmount())
                .build();
    }
}