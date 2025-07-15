package com.doordash.ordering_service.controllers;

import com.doordash.ordering_service.models.dtos.OrderRequest;
import com.doordash.ordering_service.models.dtos.OrderResponse;
import com.doordash.ordering_service.services.OrderService;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place order", description = "Places a new order using items in the customer's cart")
    @Timed(value = "order.place", description = "Time taken to place an order")
    public ResponseEntity<OrderResponse> placeOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OrderRequest request) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        request.setCustomerId(customerId);
        OrderResponse order = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order", description = "Retrieves details for a specific order")
    @Timed(value = "order.get", description = "Time taken to get an order")
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID orderId) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        OrderResponse order = orderService.getOrder(customerId, orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @Operation(summary = "Get order history", description = "Retrieves the customer's order history")
    @Timed(value = "order.get_history", description = "Time taken to get order history")
    public ResponseEntity<List<OrderResponse>> getOrderHistory(@AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        List<OrderResponse> orders = orderService.getOrderHistory(customerId);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancels an order if it's in a cancellable state")
    @Timed(value = "order.cancel", description = "Time taken to cancel an order")
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID orderId) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        OrderResponse order = orderService.cancelOrder(customerId, orderId);
        return ResponseEntity.ok(order);
    }
}