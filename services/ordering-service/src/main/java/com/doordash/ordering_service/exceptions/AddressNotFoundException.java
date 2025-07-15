package com.doordash.ordering_service.exceptions;

public class AddressNotFoundException extends ResourceNotFoundException {
    public AddressNotFoundException(String message) {
        super(message);
    }
    
    public AddressNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}