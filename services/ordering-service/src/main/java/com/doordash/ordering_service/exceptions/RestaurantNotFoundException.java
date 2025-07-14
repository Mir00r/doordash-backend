package com.doordash.ordering_service.exceptions;

import java.util.UUID;

public class RestaurantNotFoundException extends RuntimeException {
  public RestaurantNotFoundException(UUID restaurantId) {
    super(String.format("Restaurant not found with id: %s", restaurantId));
  }
}
