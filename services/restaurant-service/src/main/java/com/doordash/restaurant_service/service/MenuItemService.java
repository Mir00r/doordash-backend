package com.doordash.restaurant_service.service;

import com.doordash.restaurant_service.dto.*;
import com.doordash.restaurant_service.exceptions.ResourceNotFoundException;
import com.doordash.restaurant_service.model.MenuItem;
import com.doordash.restaurant_service.model.Restaurant;
import com.doordash.restaurant_service.repository.MenuItemRepository;
import com.doordash.restaurant_service.repository.RestaurantRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    @Cacheable(value = "menu-items", key = "#id")
    @Timed(value = "menuItem.get", description = "Time taken to get a menu item by ID")
    public MenuItemResponse getMenuItemById(Long id) {
        log.info("Getting menu item with ID: {}", id);
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        return mapToMenuItemResponse(menuItem);
    }

    @Cacheable(value = "menu-items", key = "'restaurant_' + #restaurantId + '_' + #page + '_' + #size")
    @Timed(value = "menuItem.getByRestaurant", description = "Time taken to get menu items by restaurant ID")
    public PagedResponse<MenuItemResponse> getMenuItemsByRestaurantId(Long restaurantId, int page, int size) {
        log.info("Getting menu items for restaurant ID: {}, page: {}, size: {}", restaurantId, page, size);
        // Check if restaurant exists
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant", "id", restaurantId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("category").ascending().and(Sort.by("name").ascending()));
        Page<MenuItem> menuItemPage = menuItemRepository.findByRestaurantId(restaurantId, pageable);
        return createPagedResponse(menuItemPage);
    }

    @Cacheable(value = "menu-items", key = "'restaurant_' + #restaurantId + '_search_' + #searchRequest.toString() + '_' + #page + '_' + #size")
    @Timed(value = "menuItem.search", description = "Time taken to search menu items")
    public PagedResponse<MenuItemResponse> searchMenuItems(Long restaurantId, MenuItemSearchRequest searchRequest, int page, int size) {
        log.info("Searching menu items for restaurant ID: {} with criteria: {}, page: {}, size: {}", 
                restaurantId, searchRequest, page, size);
        // Check if restaurant exists
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant", "id", restaurantId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("category").ascending().and(Sort.by("name").ascending()));
        Page<MenuItem> menuItemPage = menuItemRepository.searchMenuItems(
                restaurantId,
                searchRequest.getName(),
                searchRequest.getCategory(),
                searchRequest.getAvailable(),
                searchRequest.getVegetarian(),
                searchRequest.getVegan(),
                searchRequest.getGlutenFree(),
                searchRequest.getSpicy(),
                pageable);
        return createPagedResponse(menuItemPage);
    }

    @Transactional
    @CacheEvict(value = "menu-items", allEntries = true)
    @Timed(value = "menuItem.create", description = "Time taken to create a menu item")
    public MenuItemResponse createMenuItem(Long restaurantId, MenuItemRequest menuItemRequest) {
        log.info("Creating new menu item for restaurant ID: {}: {}", restaurantId, menuItemRequest.getName());
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        MenuItem menuItem = mapToMenuItem(menuItemRequest);
        menuItem.setRestaurant(restaurant);
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        return mapToMenuItemResponse(savedMenuItem);
    }

    @Transactional
    @CacheEvict(value = "menu-items", key = "#id")
    @Timed(value = "menuItem.update", description = "Time taken to update a menu item")
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest menuItemRequest) {
        log.info("Updating menu item with ID: {}", id);
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));

        // Update menu item fields
        menuItem.setName(menuItemRequest.getName());
        menuItem.setDescription(menuItemRequest.getDescription());
        menuItem.setPrice(menuItemRequest.getPrice());
        menuItem.setCategory(menuItemRequest.getCategory());
        menuItem.setImageUrl(menuItemRequest.getImageUrl());
        menuItem.setAvailable(menuItemRequest.getAvailable());
        menuItem.setPreparationTime(menuItemRequest.getPreparationTime());
        menuItem.setVegetarian(menuItemRequest.getVegetarian());
        menuItem.setVegan(menuItemRequest.getVegan());
        menuItem.setGlutenFree(menuItemRequest.getGlutenFree());
        menuItem.setSpicy(menuItemRequest.getSpicy());

        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        return mapToMenuItemResponse(updatedMenuItem);
    }

    @Transactional
    @CacheEvict(value = "menu-items", key = "#id")
    @Timed(value = "menuItem.delete", description = "Time taken to delete a menu item")
    public void deleteMenuItem(Long id) {
        log.info("Deleting menu item with ID: {}", id);
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        menuItemRepository.delete(menuItem);
    }

    @Transactional
    @CacheEvict(value = "menu-items", allEntries = true)
    @Timed(value = "menuItem.toggleAvailability", description = "Time taken to toggle menu item availability")
    public MenuItemResponse toggleMenuItemAvailability(Long id) {
        log.info("Toggling availability for menu item with ID: {}", id);
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        menuItem.setAvailable(!menuItem.getAvailable());
        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        return mapToMenuItemResponse(updatedMenuItem);
    }

    // Helper methods for mapping between entities and DTOs
    private MenuItem mapToMenuItem(MenuItemRequest menuItemRequest) {
        return MenuItem.builder()
                .name(menuItemRequest.getName())
                .description(menuItemRequest.getDescription())
                .price(menuItemRequest.getPrice())
                .category(menuItemRequest.getCategory())
                .imageUrl(menuItemRequest.getImageUrl())
                .available(menuItemRequest.getAvailable())
                .preparationTime(menuItemRequest.getPreparationTime())
                .vegetarian(menuItemRequest.getVegetarian())
                .vegan(menuItemRequest.getVegan())
                .glutenFree(menuItemRequest.getGlutenFree())
                .spicy(menuItemRequest.getSpicy())
                .build();
    }

    private MenuItemResponse mapToMenuItemResponse(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .category(menuItem.getCategory())
                .imageUrl(menuItem.getImageUrl())
                .available(menuItem.getAvailable())
                .preparationTime(menuItem.getPreparationTime())
                .vegetarian(menuItem.getVegetarian())
                .vegan(menuItem.getVegan())
                .glutenFree(menuItem.getGlutenFree())
                .spicy(menuItem.getSpicy())
                .restaurantId(menuItem.getRestaurant().getId())
                .restaurantName(menuItem.getRestaurant().getName())
                .createdAt(menuItem.getCreatedAt())
                .updatedAt(menuItem.getUpdatedAt())
                .build();
    }

    private PagedResponse<MenuItemResponse> createPagedResponse(Page<MenuItem> menuItemPage) {
        List<MenuItemResponse> content = menuItemPage.getContent().stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());

        return PagedResponse.<MenuItemResponse>builder()
                .content(content)
                .page(menuItemPage.getNumber())
                .size(menuItemPage.getSize())
                .totalElements(menuItemPage.getTotalElements())
                .totalPages(menuItemPage.getTotalPages())
                .last(menuItemPage.isLast())
                .build();
    }
}