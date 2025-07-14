package com.doordash.ordering_service.exceptions;

import java.util.UUID;

public class CartNotFoundException extends RuntimeException {
  public CartNotFoundException(UUID cartId) {
    super(String.format("Cart not found with id: %s", cartId));
  }
}

