package com.doordash.order_service.clients;

import com.doordash.order_service.models.dtos.CartResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "cart-service", url = "${app.cart-service.url}")
public interface CartClient {

    @GetMapping("/api/v1/cart/{cartId}")
    @CircuitBreaker(name = "cartService", fallbackMethod = "getCartFallback")
    @Retry(name = "cartService")
    CartResponse getCart(@PathVariable("cartId") UUID cartId, @RequestHeader("X-Customer-ID") UUID customerId);

    @DeleteMapping("/api/v1/cart/{cartId}")
    @CircuitBreaker(name = "cartService", fallbackMethod = "clearCartFallback")
    @Retry(name = "cartService")
    void clearCart(@PathVariable("cartId") UUID cartId, @RequestHeader("X-Customer-ID") UUID customerId);

    default CartResponse getCartFallback(UUID cartId, UUID customerId, Exception ex) {
        throw new RuntimeException("Cart service is unavailable", ex);
    }

    default void clearCartFallback(UUID cartId, UUID customerId, Exception ex) {
        throw new RuntimeException("Cart service is unavailable", ex);
    }
}