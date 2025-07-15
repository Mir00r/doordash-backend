package com.doordash.cart_service.clients;

import com.doordash.cart_service.models.dtos.MenuItemResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "restaurant-service", url = "${app.restaurant-service.url}")
public interface RestaurantClient {

    @GetMapping("/api/v1/restaurants/{restaurantId}/menu/{menuItemId}")
    @CircuitBreaker(name = "restaurantService", fallbackMethod = "getMenuItemFallback")
    @Retry(name = "restaurantService")
    MenuItemResponse getMenuItem(@PathVariable("restaurantId") UUID restaurantId, @PathVariable("menuItemId") UUID menuItemId);

    default MenuItemResponse getMenuItemFallback(UUID restaurantId, UUID menuItemId, Exception ex) {
        throw new RuntimeException("Restaurant service is unavailable", ex);
    }
}