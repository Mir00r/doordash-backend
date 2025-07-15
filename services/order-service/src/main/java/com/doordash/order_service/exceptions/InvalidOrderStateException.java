package com.doordash.order_service.exceptions;

public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String message) {
        super(message);
    }

    public InvalidOrderStateException(String orderId, String currentState, String requestedAction) {
        super(String.format("Cannot perform '%s' on order %s with current state '%s'", requestedAction, orderId, currentState));
    }
}