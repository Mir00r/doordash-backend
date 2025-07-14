package com.doordash.ordering_service.exceptions;

import com.doordash.ordering_service.enums.OrderStatus;

public class OrderCancellationException extends RuntimeException {
  public OrderCancellationException(OrderStatus currentStatus) {
    super(String.format("Order cannot be cancelled in current status: %s", currentStatus));
  }
}
