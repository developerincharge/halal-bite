package com.halalbite.orderservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
class GlobalExceptionHandler {

    record ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {}

    @ExceptionHandler(OrderExceptions.OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(OrderExceptions.OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, "Not Found", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(OrderExceptions.InvalidOrderStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(
            OrderExceptions.InvalidOrderStatusException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(400, "Bad Request", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(OrderExceptions.MenuItemUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleUnavailable(
            OrderExceptions.MenuItemUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ErrorResponse(422, "Item Unavailable", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(OrderExceptions.OrderNotCancellableException.class)
    public ResponseEntity<ErrorResponse> handleNotCancellable(
            OrderExceptions.OrderNotCancellableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(400, "Cannot Cancel", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e ->
            errors.put(((FieldError) e).getField(), e.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error in order-service", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(500, "Internal Server Error",
                "An unexpected error occurred", LocalDateTime.now()));
    }
}
