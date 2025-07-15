package com.doordash.ordering_service.controllers;

import com.doordash.ordering_service.models.dtos.AddToCartRequest;
import com.doordash.ordering_service.models.dtos.CartDTO;
import com.doordash.ordering_service.models.dtos.UpdateCartItemRequest;
import com.doordash.ordering_service.services.CartService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Cart management APIs")
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Adds a menu item to the customer's cart")
    @Timed(value = "cart.add_item", description = "Time taken to add an item to cart")
    public ResponseEntity<CartDTO> addToCart(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddToCartRequest request) {
        Long customerId = Long.parseLong(jwt.getSubject());
        CartDTO cart = cartService.addToCart(customerId, request.getRestaurantId(), request.getMenuItemId(), request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    @PutMapping("/items")
    @Operation(summary = "Update cart item", description = "Updates the quantity of a menu item in the cart")
    @Timed(value = "cart.update_item", description = "Time taken to update a cart item")
    public ResponseEntity<CartDTO> updateCartItem(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateCartItemRequest request) {
        Long customerId = Long.parseLong(jwt.getSubject());
        CartDTO cart = cartService.updateCartItem(customerId, request.getMenuItemId(), request.getQuantity());
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{menuItemId}")
    @Operation(summary = "Remove item from cart", description = "Removes a menu item from the cart")
    @Timed(value = "cart.remove_item", description = "Time taken to remove an item from cart")
    public ResponseEntity<CartDTO> removeFromCart(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long menuItemId) {
        Long customerId = Long.parseLong(jwt.getSubject());
        CartDTO cart = cartService.removeFromCart(customerId, menuItemId);
        return ResponseEntity.ok(cart);
    }

    @GetMapping
    @Operation(summary = "Get cart", description = "Retrieves the customer's current cart")
    @Timed(value = "cart.get", description = "Time taken to get cart")
    public ResponseEntity<CartDTO> getCart(@AuthenticationPrincipal Jwt jwt) {
        Long customerId = Long.parseLong(jwt.getSubject());
        CartDTO cart = cartService.getCart(customerId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Removes all items from the customer's cart")
    @Timed(value = "cart.clear", description = "Time taken to clear cart")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal Jwt jwt) {
        Long customerId = Long.parseLong(jwt.getSubject());
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}