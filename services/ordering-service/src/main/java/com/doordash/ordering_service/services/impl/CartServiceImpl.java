package com.doordash.ordering_service.services.impl;

import com.doordash.ordering_service.exceptions.CartNotFoundException;
import com.doordash.ordering_service.exceptions.RestaurantNotFoundException;
import com.doordash.ordering_service.models.dtos.CartItemResponse;
import com.doordash.ordering_service.models.dtos.CartRequest;
import com.doordash.ordering_service.models.dtos.CartResponse;
import com.doordash.ordering_service.models.entities.Cart;
import com.doordash.ordering_service.models.entities.CartItems;
import com.doordash.ordering_service.repositories.CartRepository;
import com.doordash.ordering_service.services.CartService;
import com.doordash.ordering_service.services.RestaurantService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final RestaurantService restaurantService;
    private final MeterRegistry meterRegistry;

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#cartRequest.customerId")
    public CartResponse addToCart(CartRequest cartRequest) {
        log.info("Adding item to cart for customer: {}", cartRequest.getCustomerId());
        meterRegistry.counter("cart.add_item").increment();
        
        // Verify restaurant exists
        var restaurantInfo = restaurantService.getRestaurantInfo(cartRequest.getRestaurantId());
        if (restaurantInfo == null) {
            throw new RestaurantNotFoundException("Restaurant not found with ID: " + cartRequest.getRestaurantId());
        }
        
        // Get menu item details from restaurant service
        var menuItem = restaurantService.getMenuItem(cartRequest.getRestaurantId(), cartRequest.getMenuItemId());
        if (menuItem == null) {
            throw new RestaurantNotFoundException("Menu item not found with ID: " + cartRequest.getMenuItemId());
        }
        
        // Check if cart exists for customer
        var cartOptional = cartRepository.findByCustomerId(cartRequest.getCustomerId());
        
        Cart cart;
        if (cartOptional.isPresent()) {
            cart = cartOptional.get();
            
            // If cart has items from a different restaurant, clear it first
            if (!cart.getRestaurantId().equals(cartRequest.getRestaurantId())) {
                cart.setItems(new CartItems());
                cart.setRestaurantId(cartRequest.getRestaurantId());
            }
        } else {
            // Create new cart
            cart = Cart.builder()
                    .customerId(cartRequest.getCustomerId())
                    .restaurantId(cartRequest.getRestaurantId())
                    .items(new CartItems())
                    .totalAmount(0.0)
                    .build();
        }
        
        // Add item to cart
        CartItems.CartItem cartItem = CartItems.CartItem.builder()
                .menuItemId(cartRequest.getMenuItemId())
                .name(menuItem.getName())
                .price(menuItem.getPrice())
                .quantity(cartRequest.getQuantity())
                .imageUrl(menuItem.getImageUrl())
                .build();
        
        if (cart.getItems() == null) {
            cart.setItems(new CartItems());
        }
        
        cart.getItems().addItem(cartItem);
        cart.setTotalAmount(cart.getItems().calculateTotal());
        
        // Save cart
        cart = cartRepository.save(cart);
        
        return mapToCartResponse(cart, restaurantInfo.getName());
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#customerId")
    public CartResponse updateCartItem(UUID customerId, UUID menuItemId, int quantity) {
        log.info("Updating cart item for customer: {}, menuItem: {}, quantity: {}", customerId, menuItemId, quantity);
        meterRegistry.counter("cart.update_item").increment();
        
        Cart cart = getCartEntityOrThrow(customerId);
        
        if (quantity <= 0) {
            return removeFromCart(customerId, menuItemId);
        }
        
        cart.getItems().updateItem(menuItemId, quantity);
        cart.setTotalAmount(cart.getItems().calculateTotal());
        cart = cartRepository.save(cart);
        
        var restaurantInfo = restaurantService.getRestaurantInfo(cart.getRestaurantId());
        return mapToCartResponse(cart, restaurantInfo.getName());
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#customerId")
    public CartResponse removeFromCart(UUID customerId, UUID menuItemId) {
        log.info("Removing item from cart for customer: {}, menuItem: {}", customerId, menuItemId);
        meterRegistry.counter("cart.remove_item").increment();
        
        Cart cart = getCartEntityOrThrow(customerId);
        
        cart.getItems().removeItem(menuItemId);
        cart.setTotalAmount(cart.getItems().calculateTotal());
        
        // If cart is empty, delete it
        if (cart.getItems().getItems().isEmpty()) {
            cartRepository.delete(cart);
            return CartResponse.builder()
                    .customerId(customerId)
                    .totalAmount(0.0)
                    .items(List.of())
                    .build();
        }
        
        cart = cartRepository.save(cart);
        
        var restaurantInfo = restaurantService.getRestaurantInfo(cart.getRestaurantId());
        return mapToCartResponse(cart, restaurantInfo.getName());
    }

    @Override
    @Cacheable(value = "cart", key = "#customerId")
    public CartResponse getCart(UUID customerId) {
        log.info("Getting cart for customer: {}", customerId);
        meterRegistry.counter("cart.get").increment();
        
        var cartOptional = cartRepository.findByCustomerId(customerId);
        
        if (cartOptional.isEmpty()) {
            return CartResponse.builder()
                    .customerId(customerId)
                    .totalAmount(0.0)
                    .items(List.of())
                    .build();
        }
        
        var cart = cartOptional.get();
        var restaurantInfo = restaurantService.getRestaurantInfo(cart.getRestaurantId());
        
        return mapToCartResponse(cart, restaurantInfo.getName());
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#customerId")
    public void clearCart(UUID customerId) {
        log.info("Clearing cart for customer: {}", customerId);
        meterRegistry.counter("cart.clear").increment();
        
        cartRepository.deleteByCustomerId(customerId);
    }

    @Override
    public Cart getCartEntity(UUID customerId) {
        return getCartEntityOrThrow(customerId);
    }
    
    private Cart getCartEntityOrThrow(UUID customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for customer: " + customerId));
    }
    
    private CartResponse mapToCartResponse(Cart cart, String restaurantName) {
        List<CartItemResponse> itemResponses = cart.getItems().getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .menuItemId(item.getMenuItemId())
                        .name(item.getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subtotal(item.getPrice() * item.getQuantity())
                        .imageUrl(item.getImageUrl())
                        .build())
                .collect(Collectors.toList());
        
        return CartResponse.builder()
                .cartId(cart.getId())
                .customerId(cart.getCustomerId())
                .restaurantId(cart.getRestaurantId())
                .restaurantName(restaurantName)
                .items(itemResponses)
                .totalAmount(cart.getTotalAmount())
                .build();
    }
}