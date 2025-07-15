package com.doordash.order_service.clients;

import com.doordash.order_service.models.dtos.RestaurantResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "restaurant-service", url = "${app.restaurant-service.url}")
public interface RestaurantClient {

    @GetMapping("/api/v1/restaurants/{restaurantId}")
    @CircuitBreaker(name = "restaurantService", fallbackMethod = "getRestaurantInfoFallback")
    @Retry(name = "restaurantService")
    RestaurantResponse getRestaurantInfo(@PathVariable("restaurantId") UUID restaurantId);

    default RestaurantResponse getRestaurantInfoFallback(UUID restaurantId, Exception ex) {
        return RestaurantResponse.builder()
                .id(restaurantId)
                .name("Restaurant information unavailable")
                .build();
    }
}