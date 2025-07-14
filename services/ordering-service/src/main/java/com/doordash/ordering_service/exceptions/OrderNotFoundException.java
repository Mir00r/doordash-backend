package com.doordash.ordering_service.exceptions;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {
  public OrderNotFoundException(UUID orderId) {
    super(String.format("Order not found with id: %s", orderId));
  }
}
