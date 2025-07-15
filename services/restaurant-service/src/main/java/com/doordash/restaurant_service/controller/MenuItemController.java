package com.doordash.restaurant_service.controller;

import com.doordash.restaurant_service.dto.*;
import com.doordash.restaurant_service.service.MenuItemService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Menu Item API", description = "Endpoints for managing menu items")
public class MenuItemController {

    private final MenuItemService menuItemService;

    @GetMapping("/menu-items/{id}")
    @Operation(summary = "Get menu item by ID", description = "Returns a menu item by its ID")
    @ApiResponse(responseCode = "200", description = "Successful operation", 
            content = @Content(schema = @Schema(implementation = MenuItemResponse.class)))
    @ApiResponse(responseCode = "404", description = "Menu item not found")
    @Timed(value = "api.menuItems.getById", description = "Time taken to get a menu item by ID")
    public ResponseEntity<MenuItemResponse> getMenuItemById(
            @Parameter(description = "Menu item ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.getMenuItemById(id));
    }

    @GetMapping("/restaurants/{restaurantId}/menu-items")
    @Operation(summary = "Get menu items by restaurant ID", description = "Returns a paginated list of menu items for a restaurant")
    @ApiResponse(responseCode = "200", description = "Successful operation", 
            content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @Timed(value = "api.menuItems.getByRestaurant", description = "Time taken to get menu items by restaurant ID")
    public ResponseEntity<PagedResponse<MenuItemResponse>> getMenuItemsByRestaurantId(
            @Parameter(description = "Restaurant ID", required = true) @PathVariable Long restaurantId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(menuItemService.getMenuItemsByRestaurantId(restaurantId, page, size));
    }

    @PostMapping("/restaurants/{restaurantId}/menu-items/search")
    @Operation(summary = "Search menu items", description = "Returns a paginated list of menu items matching the search criteria")
    @ApiResponse(responseCode = "200", description = "Successful operation", 
            content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @Timed(value = "api.menuItems.search", description = "Time taken to search menu items")
    public ResponseEntity<PagedResponse<MenuItemResponse>> searchMenuItems(
            @Parameter(description = "Restaurant ID", required = true) @PathVariable Long restaurantId,
            @Parameter(description = "Search criteria", required = true) @RequestBody MenuItemSearchRequest searchRequest,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(menuItemService.searchMenuItems(restaurantId, searchRequest, page, size));
    }

    @PostMapping("/restaurants/{restaurantId}/menu-items")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new menu item", description = "Creates a new menu item for a restaurant", 
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "Menu item created successfully", 
            content = @Content(schema = @Schema(implementation = MenuItemResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @Timed(value = "api.menuItems.create", description = "Time taken to create a menu item")
    public ResponseEntity<MenuItemResponse> createMenuItem(
            @Parameter(description = "Restaurant ID", required = true) @PathVariable Long restaurantId,
            @Parameter(description = "Menu item details", required = true) @Valid @RequestBody MenuItemRequest menuItemRequest,
            @AuthenticationPrincipal Jwt jwt) {
        return new ResponseEntity<>(menuItemService.createMenuItem(restaurantId, menuItemRequest), HttpStatus.CREATED);
    }

    @PutMapping("/menu-items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a menu item", description = "Updates an existing menu item", 
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Menu item updated successfully", 
            content = @Content(schema = @Schema(implementation = MenuItemResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Menu item not found")
    @Timed(value = "api.menuItems.update", description = "Time taken to update a menu item")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @Parameter(description = "Menu item ID", required = true) @PathVariable Long id,
            @Parameter(description = "Menu item details", required = true) @Valid @RequestBody MenuItemRequest menuItemRequest,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(menuItemService.updateMenuItem(id, menuItemRequest));
    }

    @DeleteMapping("/menu-items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a menu item", description = "Deletes an existing menu item", 
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Menu item deleted successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Menu item not found")
    @Timed(value = "api.menuItems.delete", description = "Time taken to delete a menu item")
    public ResponseEntity<Void> deleteMenuItem(
            @Parameter(description = "Menu item ID", required = true) @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/menu-items/{id}/toggle-availability")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle menu item availability", description = "Toggles the availability status of a menu item", 
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Menu item availability toggled successfully", 
            content = @Content(schema = @Schema(implementation = MenuItemResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Menu item not found")
    @Timed(value = "api.menuItems.toggleAvailability", description = "Time taken to toggle menu item availability")
    public ResponseEntity<MenuItemResponse> toggleMenuItemAvailability(
            @Parameter(description = "Menu item ID", required = true) @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(menuItemService.toggleMenuItemAvailability(id));
    }
}