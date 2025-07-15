package com.doordash.order_service.controllers;

import com.doordash.order_service.models.dtos.OrderRequest;
import com.doordash.order_service.models.dtos.OrderResponse;
import com.doordash.order_service.services.OrderService;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order API", description = "API endpoints for order management")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Place a new order", description = "Places a new order for the authenticated customer")
    @Timed(value = "order.place", description = "Time taken to place an order")
    public ResponseEntity<OrderResponse> placeOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OrderRequest orderRequest) {
        
        UUID customerId = UUID.fromString(jwt.getSubject());
        orderRequest.setCustomerId(customerId);
        
        OrderResponse response = orderService.placeOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details", description = "Retrieves details of a specific order for the authenticated customer")
    @Timed(value = "order.get", description = "Time taken to get an order")
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID orderId) {
        
        UUID customerId = UUID.fromString(jwt.getSubject());
        OrderResponse response = orderService.getOrder(customerId, orderId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Cancel an order", description = "Cancels a pending order for the authenticated customer")
    @Timed(value = "order.cancel", description = "Time taken to cancel an order")
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID orderId) {
        
        UUID customerId = UUID.fromString(jwt.getSubject());
        OrderResponse response = orderService.cancelOrder(customerId, orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get order history", description = "Retrieves order history for the authenticated customer")
    @Timed(value = "order.history", description = "Time taken to get order history")
    public ResponseEntity<List<OrderResponse>> getOrderHistory(
            @AuthenticationPrincipal Jwt jwt) {
        
        UUID customerId = UUID.fromString(jwt.getSubject());
        List<OrderResponse> response = orderService.getOrderHistory(customerId);
        return ResponseEntity.ok(response);
    }
}