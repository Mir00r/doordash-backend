package com.doordash.ordering_service.exceptions;

public class PaymentMethodNotFoundException extends ResourceNotFoundException {
    public PaymentMethodNotFoundException(String message) {
        super(message);
    }
    
    public PaymentMethodNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}