package com.doordash.ordering_service.exceptions;

public class MenuItemNotFoundException extends ResourceNotFoundException {
    public MenuItemNotFoundException(String message) {
        super(message);
    }
    
    public MenuItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}