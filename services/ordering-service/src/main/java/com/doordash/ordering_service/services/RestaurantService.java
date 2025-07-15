package com.doordash.ordering_service.services;

import com.doordash.ordering_service.models.dtos.*;

import java.util.List;
import java.util.UUID;

public interface RestaurantService {
    /**
     * Search for restaurants based on search criteria
     * @param searchRequest the search request containing search criteria
     * @return the search response with restaurant results
     */
    SearchResponse searchRestaurants(SearchRequest searchRequest);
    
    /**
     * Get restaurant information by ID
     * @param restaurantId the restaurant ID
     * @return the restaurant response
     */
    RestaurantResponse getRestaurantInfo(UUID restaurantId);
    
    /**
     * Get menu items for a restaurant
     * @param restaurantId the restaurant ID
     * @return list of menu item responses
     */
    List<MenuItemResponse> getMenuItems(UUID restaurantId);
    
    /**
     * Get a specific menu item
     * @param restaurantId the restaurant ID
     * @param menuItemId the menu item ID
     * @return the menu item response
     */
    MenuItemResponse getMenuItem(UUID restaurantId, UUID menuItemId);
}