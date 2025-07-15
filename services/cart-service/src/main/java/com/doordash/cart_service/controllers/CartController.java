package com.doordash.cart_service.controllers;

import com.doordash.cart_service.models.dtos.AddCartItemRequest;
import com.doordash.cart_service.models.dtos.CartResponse;
import com.doordash.cart_service.models.dtos.UpdateCartItemRequest;
import com.doordash.cart_service.services.CartService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart API", description = "API endpoints for cart management")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get cart", description = "Retrieves the current cart for the authenticated customer")
    @Timed(value = "cart.get", description = "Time taken to get a cart")
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        CartResponse response = cartService.getCart(customerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add item to cart", description = "Adds a new item to the cart for the authenticated customer")
    @Timed(value = "cart.add_item", description = "Time taken to add an item to cart")
    public ResponseEntity<CartResponse> addItemToCart(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddCartItemRequest request) {
        
        UUID customerId = UUID.fromString(jwt.getSubject());
        CartResponse response = cartService.addItemToCart(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/items")
    @Operation(summary = "Update cart item", description = "Updates the quantity of an item in the cart for the authenticated customer")
    @Timed(value = "cart.update_item", description = "Time taken to update an item in cart")
    public ResponseEntity<CartResponse> updateCartItem(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateCartItemRequest request) {
        
        UUID customerId = UUID.fromString(jwt.getSubject());
        CartResponse response = cartService.updateCartItem(customerId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{menuItemId}")
    @Operation(summary = "Remove cart item", description = "Removes an item from the cart for the authenticated customer")
    @Timed(value = "cart.remove_item", description = "Time taken to remove an item from cart")
    public ResponseEntity<CartResponse> removeCartItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID menuItemId) {
        
        UUID customerId = UUID.fromString(jwt.getSubject());
        CartResponse response = cartService.removeCartItem(customerId, menuItemId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Clears all items from the cart for the authenticated customer")
    @Timed(value = "cart.clear", description = "Time taken to clear cart")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}