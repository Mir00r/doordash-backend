package com.doordash.ordering_service.services.impl;

import com.doordash.ordering_service.exceptions.RestaurantNotFoundException;
import com.doordash.ordering_service.models.dtos.*;
import com.doordash.ordering_service.services.RestaurantService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantServiceImpl implements RestaurantService {

    private final WebClient.Builder webClientBuilder;
    private final MeterRegistry meterRegistry;
    
    @Value("${restaurant-service.url:http://restaurant-service:8080}")
    private String restaurantServiceUrl;
    
    @Value("${restaurant-service.search-path:/api/v1/restaurants/search}")
    private String searchPath;
    
    @Value("${restaurant-service.restaurant-path:/api/v1/restaurants}")
    private String restaurantPath;
    
    @Value("${restaurant-service.menu-path:/api/v1/restaurants/{restaurantId}/menu}")
    private String menuPath;
    
    @Value("${restaurant-service.menu-item-path:/api/v1/restaurants/{restaurantId}/menu/{menuItemId}}")
    private String menuItemPath;

    @Override
    @Cacheable(value = "restaurantSearch", key = "#searchRequest.toString()")
    @CircuitBreaker(name = "restaurantService", fallbackMethod = "searchRestaurantsFallback")
    @Retry(name = "restaurantService")
    public SearchResponse searchRestaurants(SearchRequest searchRequest) {
        log.info("Searching restaurants with criteria: {}", searchRequest);
        meterRegistry.counter("restaurant.search").increment();
        
        try {
            return webClientBuilder.build()
                    .post()
                    .uri(restaurantServiceUrl + searchPath)
                    .bodyValue(searchRequest)
                    .retrieve()
                    .bodyToMono(SearchResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error searching restaurants: {}", e.getMessage());
            meterRegistry.counter("restaurant.search.error").increment();
            return searchRestaurantsFallback(searchRequest, e);
        } catch (Exception e) {
            log.error("Unexpected error searching restaurants: {}", e.getMessage());
            meterRegistry.counter("restaurant.search.error").increment();
            return searchRestaurantsFallback(searchRequest, e);
        }
    }

    @Override
    @Cacheable(value = "restaurantInfo", key = "#restaurantId")
    @CircuitBreaker(name = "restaurantService", fallbackMethod = "getRestaurantInfoFallback")
    @Retry(name = "restaurantService")
    public RestaurantResponse getRestaurantInfo(UUID restaurantId) {
        log.info("Getting restaurant info for ID: {}", restaurantId);
        meterRegistry.counter("restaurant.get_info").increment();
        
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(restaurantServiceUrl + restaurantPath + "/{id}", restaurantId)
                    .retrieve()
                    .bodyToMono(RestaurantResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error getting restaurant info: {}", e.getMessage());
            meterRegistry.counter("restaurant.get_info.error").increment();
            
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RestaurantNotFoundException("Restaurant not found with ID: " + restaurantId);
            }
            
            return getRestaurantInfoFallback(restaurantId, e);
        } catch (Exception e) {
            log.error("Unexpected error getting restaurant info: {}", e.getMessage());
            meterRegistry.counter("restaurant.get_info.error").increment();
            return getRestaurantInfoFallback(restaurantId, e);
        }
    }

    @Override
    @Cacheable(value = "menuItems", key = "#restaurantId")
    @CircuitBreaker(name = "restaurantService", fallbackMethod = "getMenuItemsFallback")
    @Retry(name = "restaurantService")
    public List<MenuItemResponse> getMenuItems(UUID restaurantId) {
        log.info("Getting menu items for restaurant ID: {}", restaurantId);
        meterRegistry.counter("restaurant.get_menu_items").increment();
        
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(restaurantServiceUrl + menuPath, restaurantId)
                    .retrieve()
                    .bodyToFlux(MenuItemResponse.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error getting menu items: {}", e.getMessage());
            meterRegistry.counter("restaurant.get_menu_items.error").increment();
            
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RestaurantNotFoundException("Restaurant not found with ID: " + restaurantId);
            }
            
            return getMenuItemsFallback(restaurantId, e);
        } catch (Exception e) {
            log.error("Unexpected error getting menu items: {}", e.getMessage());
            meterRegistry.counter("restaurant.get_menu_items.error").increment();
            return getMenuItemsFallback(restaurantId, e);
        }
    }

    @Override
    @Cacheable(value = "menuItem", key = "#restaurantId + '-' + #menuItemId")
    @CircuitBreaker(name = "restaurantService", fallbackMethod = "getMenuItemFallback")
    @Retry(name = "restaurantService")
    public MenuItemResponse getMenuItem(UUID restaurantId, UUID menuItemId) {
        log.info("Getting menu item for restaurant ID: {}, menu item ID: {}", restaurantId, menuItemId);
        meterRegistry.counter("restaurant.get_menu_item").increment();
        
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(restaurantServiceUrl + menuItemPath, restaurantId, menuItemId)
                    .retrieve()
                    .bodyToMono(MenuItemResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error getting menu item: {}", e.getMessage());
            meterRegistry.counter("restaurant.get_menu_item.error").increment();
            
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RestaurantNotFoundException("Menu item not found with ID: " + menuItemId);
            }
            
            return getMenuItemFallback(restaurantId, menuItemId, e);
        } catch (Exception e) {
            log.error("Unexpected error getting menu item: {}", e.getMessage());
            meterRegistry.counter("restaurant.get_menu_item.error").increment();
            return getMenuItemFallback(restaurantId, menuItemId, e);
        }
    }
    
    // Fallback methods
    private SearchResponse searchRestaurantsFallback(SearchRequest searchRequest, Throwable t) {
        log.warn("Fallback for searchRestaurants: {}", t.getMessage());
        return SearchResponse.builder()
                .restaurants(Collections.emptyList())
                .totalResults(0)
                .page(searchRequest.getPage())
                .size(searchRequest.getSize())
                .totalPages(0)
                .build();
    }
    
    private RestaurantResponse getRestaurantInfoFallback(UUID restaurantId, Throwable t) {
        log.warn("Fallback for getRestaurantInfo: {}", t.getMessage());
        return RestaurantResponse.builder()
                .restaurantId(restaurantId)
                .name("Restaurant information temporarily unavailable")
                .build();
    }
    
    private List<MenuItemResponse> getMenuItemsFallback(UUID restaurantId, Throwable t) {
        log.warn("Fallback for getMenuItems: {}", t.getMessage());
        return Collections.emptyList();
    }
    
    private MenuItemResponse getMenuItemFallback(UUID restaurantId, UUID menuItemId, Throwable t) {
        log.warn("Fallback for getMenuItem: {}", t.getMessage());
        return MenuItemResponse.builder()
                .menuItemId(menuItemId)
                .name("Menu item temporarily unavailable")
                .price(0.0)
                .build();
    }
}