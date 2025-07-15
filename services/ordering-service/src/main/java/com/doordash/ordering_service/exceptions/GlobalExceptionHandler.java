package com.doordash.ordering_service.exceptions;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private final MeterRegistry meterRegistry;
  private static final String ERROR_LOG_FORMAT = "Error ID: {} | Error: {} | Path: {}";

  public GlobalExceptionHandler(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  // ========== 404 Not Found Exceptions ==========
  @ExceptionHandler({
    OrderNotFoundException.class,
    CartNotFoundException.class,
    RestaurantNotFoundException.class,
    CustomerNotFoundException.class,
    PaymentMethodNotFoundException.class,
    AddressNotFoundException.class,
    MenuItemNotFoundException.class
  })
  public ResponseEntity<ErrorResponse> handleNotFoundException(
    RuntimeException ex,
    HttpServletRequest request) {

    String errorId = UUID.randomUUID().toString();
    log.warn(ERROR_LOG_FORMAT, errorId, ex.getMessage(), request.getRequestURI());
    meterRegistry.counter("order_service.errors", "type", "not_found").increment();

    ErrorResponse errorResponse = ErrorResponse.builder()
      .errorId(errorId)
      .timestamp(Instant.now())
      .status(HttpStatus.NOT_FOUND.value())
      .error(HttpStatus.NOT_FOUND.getReasonPhrase())
      .message(ex.getMessage())
      .path(request.getRequestURI())
      .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  // ========== 400 Business Rule Violations ==========
  @ExceptionHandler({
    OrderCancellationException.class,
    PaymentProcessingException.class,
    InvalidOrderStateException.class,
    PaymentException.class
  })
  public ResponseEntity<ErrorResponse> handleBusinessException(
    RuntimeException ex,
    HttpServletRequest request) {

    String errorId = UUID.randomUUID().toString();
    log.warn(ERROR_LOG_FORMAT, errorId, ex.getMessage(), request.getRequestURI());
    meterRegistry.counter("order_service.errors", "type", "business_rule").increment();

    ErrorResponse errorResponse = ErrorResponse.builder()
      .errorId(errorId)
      .timestamp(Instant.now())
      .status(HttpStatus.BAD_REQUEST.value())
      .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
      .message(ex.getMessage())
      .path(request.getRequestURI())
      .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  // ========== 400 Validation Errors ==========
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
    MethodArgumentNotValidException ex,
    HttpServletRequest request) {

    String errorId = UUID.randomUUID().toString();
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    log.warn(ERROR_LOG_FORMAT, errorId, errors.toString(), request.getRequestURI());
    meterRegistry.counter("order_service.errors", "type", "validation").increment();

    ErrorResponse errorResponse = ErrorResponse.builder()
      .errorId(errorId)
      .timestamp(Instant.now())
      .status(HttpStatus.BAD_REQUEST.value())
      .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
      .message("Validation failed")
      .details(errors)
      .path(request.getRequestURI())
      .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  // ========== 403 Access Denied ==========
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(
    AccessDeniedException ex,
    HttpServletRequest request) {

    String errorId = UUID.randomUUID().toString();
    log.warn(ERROR_LOG_FORMAT, errorId, ex.getMessage(), request.getRequestURI());
    meterRegistry.counter("order_service.errors", "type", "access_denied").increment();

    ErrorResponse errorResponse = ErrorResponse.builder()
      .errorId(errorId)
      .timestamp(Instant.now())
      .status(HttpStatus.FORBIDDEN.value())
      .error(HttpStatus.FORBIDDEN.getReasonPhrase())
      .message("Access denied")
      .path(request.getRequestURI())
      .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
  }

  // ========== 500 Internal Server Error ==========
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllUncaughtException(
    Exception ex,
    HttpServletRequest request) {

    String errorId = UUID.randomUUID().toString();
    log.error(ERROR_LOG_FORMAT, errorId, ex.getMessage(), request.getRequestURI(), ex);
    meterRegistry.counter("order_service.errors", "type", "server_error").increment();

    ErrorResponse errorResponse = ErrorResponse.builder()
      .errorId(errorId)
      .timestamp(Instant.now())
      .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
      .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
      .message("An unexpected error occurred")
      .path(request.getRequestURI())
      .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}
