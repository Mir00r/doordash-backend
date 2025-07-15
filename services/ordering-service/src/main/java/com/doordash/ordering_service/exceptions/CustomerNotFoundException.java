package com.doordash.ordering_service.exceptions;

public class CustomerNotFoundException extends ResourceNotFoundException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
    
    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}