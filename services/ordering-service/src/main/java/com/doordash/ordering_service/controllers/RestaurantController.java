package com.doordash.ordering_service.controllers;

import com.doordash.ordering_service.models.dtos.MenuItemDTO;
import com.doordash.ordering_service.models.dtos.RestaurantDTO;
import com.doordash.ordering_service.services.RestaurantService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurant", description = "Restaurant and menu APIs")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    @Operation(summary = "Search restaurants", description = "Searches for restaurants based on location and query")
    @Timed(value = "restaurant.search", description = "Time taken to search restaurants")
    public ResponseEntity<List<RestaurantDTO>> searchRestaurants(
            @RequestParam(required = false) String query,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5") Integer radius) {
        List<RestaurantDTO> restaurants = restaurantService.searchRestaurants(query, latitude, longitude, radius);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/{restaurantId}")
    @Operation(summary = "Get restaurant", description = "Retrieves details for a specific restaurant")
    @Timed(value = "restaurant.get_info", description = "Time taken to get restaurant info")
    public ResponseEntity<RestaurantDTO> getRestaurantInfo(@PathVariable Long restaurantId) {
        RestaurantDTO restaurant = restaurantService.getRestaurantInfo(restaurantId);
        return ResponseEntity.ok(restaurant);
    }

    @GetMapping("/{restaurantId}/menu")
    @Operation(summary = "Get menu", description = "Retrieves the menu for a specific restaurant")
    @Timed(value = "restaurant.get_menu", description = "Time taken to get restaurant menu")
    public ResponseEntity<List<MenuItemDTO>> getMenuItems(@PathVariable Long restaurantId) {
        List<MenuItemDTO> menuItems = restaurantService.getMenuItems(restaurantId);
        return ResponseEntity.ok(menuItems);
    }

    @GetMapping("/{restaurantId}/menu/{menuItemId}")
    @Operation(summary = "Get menu item", description = "Retrieves details for a specific menu item")
    @Timed(value = "restaurant.get_menu_item", description = "Time taken to get menu item")
    public ResponseEntity<MenuItemDTO> getMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId) {
        MenuItemDTO menuItem = restaurantService.getMenuItem(restaurantId, menuItemId);
        return ResponseEntity.ok(menuItem);
    }
}