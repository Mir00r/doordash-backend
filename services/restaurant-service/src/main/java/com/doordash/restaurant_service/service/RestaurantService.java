package com.doordash.restaurant_service.service;

import com.doordash.restaurant_service.dto.*;
import com.doordash.restaurant_service.exceptions.ResourceNotFoundException;
import com.doordash.restaurant_service.model.Restaurant;
import com.doordash.restaurant_service.model.RestaurantHours;
import com.doordash.restaurant_service.repository.RestaurantHoursRepository;
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

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantHoursRepository restaurantHoursRepository;

    @Cacheable(value = "restaurants", key = "#id")
    @Timed(value = "restaurant.get", description = "Time taken to get a restaurant by ID")
    public RestaurantResponse getRestaurantById(Long id) {
        log.info("Getting restaurant with ID: {}", id);
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
        return mapToRestaurantResponse(restaurant);
    }

    @Cacheable(value = "restaurants", key = "'all_' + #page + '_' + #size")
    @Timed(value = "restaurant.getAll", description = "Time taken to get all restaurants")
    public PagedResponse<RestaurantResponse> getAllRestaurants(int page, int size) {
        log.info("Getting all restaurants, page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Restaurant> restaurantPage = restaurantRepository.findAll(pageable);
        return createPagedResponse(restaurantPage);
    }

    @Cacheable(value = "restaurants", key = "'search_' + #searchRequest.toString() + '_' + #page + '_' + #size")
    @Timed(value = "restaurant.search", description = "Time taken to search restaurants")
    public PagedResponse<RestaurantResponse> searchRestaurants(RestaurantSearchRequest searchRequest, int page, int size) {
        log.info("Searching restaurants with criteria: {}, page: {}, size: {}", searchRequest, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Restaurant> restaurantPage = restaurantRepository.searchRestaurants(
                searchRequest.getName(),
                searchRequest.getCuisine(),
                searchRequest.getCity(),
                searchRequest.getZipCode(),
                searchRequest.getIsOpen(),
                pageable);
        return createPagedResponse(restaurantPage);
    }

    @Transactional
    @CacheEvict(value = "restaurants", allEntries = true)
    @Timed(value = "restaurant.create", description = "Time taken to create a restaurant")
    public RestaurantResponse createRestaurant(RestaurantRequest restaurantRequest) {
        log.info("Creating new restaurant: {}", restaurantRequest.getName());
        Restaurant restaurant = mapToRestaurant(restaurantRequest);
        restaurant.setAverageRating(BigDecimal.ZERO);
        restaurant.setTotalRatings(0);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        // Save restaurant hours if provided
        if (restaurantRequest.getHours() != null && !restaurantRequest.getHours().isEmpty()) {
            List<RestaurantHours> hours = restaurantRequest.getHours().stream()
                    .map(hoursRequest -> {
                        RestaurantHours restaurantHours = mapToRestaurantHours(hoursRequest);
                        restaurantHours.setRestaurant(savedRestaurant);
                        return restaurantHours;
                    })
                    .collect(Collectors.toList());
            savedRestaurant.setHours(hours);
            restaurantHoursRepository.saveAll(hours);
        }

        return mapToRestaurantResponse(savedRestaurant);
    }

    @Transactional
    @CacheEvict(value = "restaurants", key = "#id")
    @Timed(value = "restaurant.update", description = "Time taken to update a restaurant")
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest restaurantRequest) {
        log.info("Updating restaurant with ID: {}", id);
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));

        // Update restaurant fields
        restaurant.setName(restaurantRequest.getName());
        restaurant.setDescription(restaurantRequest.getDescription());
        restaurant.setAddress(restaurantRequest.getAddress());
        restaurant.setCity(restaurantRequest.getCity());
        restaurant.setState(restaurantRequest.getState());
        restaurant.setZipCode(restaurantRequest.getZipCode());
        restaurant.setPhoneNumber(restaurantRequest.getPhoneNumber());
        restaurant.setEmail(restaurantRequest.getEmail());
        restaurant.setWebsite(restaurantRequest.getWebsite());
        restaurant.setCuisine(restaurantRequest.getCuisine());
        restaurant.setDeliveryFee(restaurantRequest.getDeliveryFee());
        restaurant.setEstimatedDeliveryTime(restaurantRequest.getEstimatedDeliveryTime());
        restaurant.setIsOpen(restaurantRequest.getIsOpen());
        restaurant.setImageUrl(restaurantRequest.getImageUrl());

        // Update restaurant hours if provided
        if (restaurantRequest.getHours() != null && !restaurantRequest.getHours().isEmpty()) {
            // Remove existing hours
            restaurantHoursRepository.deleteAll(restaurant.getHours());
            restaurant.getHours().clear();

            // Add new hours
            List<RestaurantHours> hours = restaurantRequest.getHours().stream()
                    .map(hoursRequest -> {
                        RestaurantHours restaurantHours = mapToRestaurantHours(hoursRequest);
                        restaurantHours.setRestaurant(restaurant);
                        return restaurantHours;
                    })
                    .collect(Collectors.toList());
            restaurant.setHours(hours);
            restaurantHoursRepository.saveAll(hours);
        }

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return mapToRestaurantResponse(updatedRestaurant);
    }

    @Transactional
    @CacheEvict(value = "restaurants", key = "#id")
    @Timed(value = "restaurant.delete", description = "Time taken to delete a restaurant")
    public void deleteRestaurant(Long id) {
        log.info("Deleting restaurant with ID: {}", id);
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
        restaurantRepository.delete(restaurant);
    }

    @Timed(value = "restaurant.isOpen", description = "Time taken to check if a restaurant is open")
    public boolean isRestaurantOpen(Long id, DayOfWeek dayOfWeek, LocalTime time) {
        log.info("Checking if restaurant with ID: {} is open on {} at {}", id, dayOfWeek, time);
        return restaurantHoursRepository.isRestaurantOpenAt(id, dayOfWeek, time);
    }

    // Helper methods for mapping between entities and DTOs
    private Restaurant mapToRestaurant(RestaurantRequest restaurantRequest) {
        return Restaurant.builder()
                .name(restaurantRequest.getName())
                .description(restaurantRequest.getDescription())
                .address(restaurantRequest.getAddress())
                .city(restaurantRequest.getCity())
                .state(restaurantRequest.getState())
                .zipCode(restaurantRequest.getZipCode())
                .phoneNumber(restaurantRequest.getPhoneNumber())
                .email(restaurantRequest.getEmail())
                .website(restaurantRequest.getWebsite())
                .cuisine(restaurantRequest.getCuisine())
                .deliveryFee(restaurantRequest.getDeliveryFee())
                .estimatedDeliveryTime(restaurantRequest.getEstimatedDeliveryTime())
                .isOpen(restaurantRequest.getIsOpen())
                .imageUrl(restaurantRequest.getImageUrl())
                .build();
    }

    private RestaurantHours mapToRestaurantHours(RestaurantHoursRequest hoursRequest) {
        return RestaurantHours.builder()
                .dayOfWeek(hoursRequest.getDayOfWeek())
                .openTime(hoursRequest.getOpenTime())
                .closeTime(hoursRequest.getCloseTime())
                .isClosed(hoursRequest.getIsClosed())
                .build();
    }

    private RestaurantResponse mapToRestaurantResponse(Restaurant restaurant) {
        List<RestaurantHoursResponse> hoursResponses = restaurant.getHours().stream()
                .map(this::mapToRestaurantHoursResponse)
                .collect(Collectors.toList());

        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .address(restaurant.getAddress())
                .city(restaurant.getCity())
                .state(restaurant.getState())
                .zipCode(restaurant.getZipCode())
                .phoneNumber(restaurant.getPhoneNumber())
                .email(restaurant.getEmail())
                .website(restaurant.getWebsite())
                .cuisine(restaurant.getCuisine())
                .averageRating(restaurant.getAverageRating())
                .totalRatings(restaurant.getTotalRatings())
                .deliveryFee(restaurant.getDeliveryFee())
                .estimatedDeliveryTime(restaurant.getEstimatedDeliveryTime())
                .isOpen(restaurant.getIsOpen())
                .imageUrl(restaurant.getImageUrl())
                .hours(hoursResponses)
                .createdAt(restaurant.getCreatedAt())
                .updatedAt(restaurant.getUpdatedAt())
                .build();
    }

    private RestaurantHoursResponse mapToRestaurantHoursResponse(RestaurantHours hours) {
        return RestaurantHoursResponse.builder()
                .id(hours.getId())
                .dayOfWeek(hours.getDayOfWeek())
                .openTime(hours.getOpenTime())
                .closeTime(hours.getCloseTime())
                .isClosed(hours.getIsClosed())
                .build();
    }

    private PagedResponse<RestaurantResponse> createPagedResponse(Page<Restaurant> restaurantPage) {
        List<RestaurantResponse> content = restaurantPage.getContent().stream()
                .map(this::mapToRestaurantResponse)
                .collect(Collectors.toList());

        return PagedResponse.<RestaurantResponse>builder()
                .content(content)
                .page(restaurantPage.getNumber())
                .size(restaurantPage.getSize())
                .totalElements(restaurantPage.getTotalElements())
                .totalPages(restaurantPage.getTotalPages())
                .last(restaurantPage.isLast())
                .build();
    }
}