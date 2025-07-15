package com.doordash.restaurant_service.controller;

import com.doordash.restaurant_service.dto.*;
import com.doordash.restaurant_service.service.RestaurantService;
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

import java.time.DayOfWeek;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurant API", description = "Endpoints for managing restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    @Operation(summary = "Get all restaurants", description = "Returns a paginated list of all restaurants")
    @ApiResponse(responseCode = "200", description = "Successful operation", 
            content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    @Timed(value = "api.restaurants.getAll", description = "Time taken to get all restaurants")
    public ResponseEntity<PagedResponse<RestaurantResponse>> getAllRestaurants(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(restaurantService.getAllRestaurants(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant by ID", description = "Returns a restaurant by its ID")
    @ApiResponse(responseCode = "200", description = "Successful operation", 
            content = @Content(schema = @Schema(implementation = RestaurantResponse.class)))
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @Timed(value = "api.restaurants.getById", description = "Time taken to get a restaurant by ID")
    public ResponseEntity<RestaurantResponse> getRestaurantById(
            @Parameter(description = "Restaurant ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    @PostMapping("/search")
    @Operation(summary = "Search restaurants", description = "Returns a paginated list of restaurants matching the search criteria")
    @ApiResponse(responseCode = "200", description = "Successful operation", 
            content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    @Timed(value = "api.restaurants.search", description = "Time taken to search restaurants")
    public ResponseEntity<PagedResponse<RestaurantResponse>> searchRestaurants(
            @Parameter(description = "Search criteria", required = true) @RequestBody RestaurantSearchRequest searchRequest,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(restaurantService.searchRestaurants(searchRequest, page, size));
    }

    @GetMapping("/{id}/is-open")
    @Operation(summary = "Check if restaurant is open", description = "Checks if a restaurant is open at a specific time on a specific day")
    @ApiResponse(responseCode = "200", description = "Successful operation")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @Timed(value = "api.restaurants.isOpen", description = "Time taken to check if a restaurant is open")
    public ResponseEntity<Boolean> isRestaurantOpen(
            @Parameter(description = "Restaurant ID", required = true) @PathVariable Long id,
            @Parameter(description = "Day of week") @RequestParam(required = false) DayOfWeek dayOfWeek,
            @Parameter(description = "Time") @RequestParam(required = false) LocalTime time) {
        // If day or time not provided, use current day and time
        DayOfWeek day = dayOfWeek != null ? dayOfWeek : DayOfWeek.from(java.time.LocalDate.now());
        LocalTime currentTime = time != null ? time : LocalTime.now();
        return ResponseEntity.ok(restaurantService.isRestaurantOpen(id, day, currentTime));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new restaurant", description = "Creates a new restaurant", 
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "Restaurant created successfully", 
            content = @Content(schema = @Schema(implementation = RestaurantResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @Timed(value = "api.restaurants.create", description = "Time taken to create a restaurant")
    public ResponseEntity<RestaurantResponse> createRestaurant(
            @Parameter(description = "Restaurant details", required = true) @Valid @RequestBody RestaurantRequest restaurantRequest,
            @AuthenticationPrincipal Jwt jwt) {
        return new ResponseEntity<>(restaurantService.createRestaurant(restaurantRequest), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a restaurant", description = "Updates an existing restaurant", 
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Restaurant updated successfully", 
            content = @Content(schema = @Schema(implementation = RestaurantResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @Timed(value = "api.restaurants.update", description = "Time taken to update a restaurant")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @Parameter(description = "Restaurant ID", required = true) @PathVariable Long id,
            @Parameter(description = "Restaurant details", required = true) @Valid @RequestBody RestaurantRequest restaurantRequest,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, restaurantRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a restaurant", description = "Deletes an existing restaurant", 
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Restaurant deleted successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @Timed(value = "api.restaurants.delete", description = "Time taken to delete a restaurant")
    public ResponseEntity<Void> deleteRestaurant(
            @Parameter(description = "Restaurant ID", required = true) @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }
}