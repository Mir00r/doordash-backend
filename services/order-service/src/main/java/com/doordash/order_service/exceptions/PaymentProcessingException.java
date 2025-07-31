package com.doordash.order_service.exceptions;

public class PaymentProcessingException extends RuntimeException {

  public PaymentProcessingException(String message) {
    super(message);
  }

  public PaymentProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}
