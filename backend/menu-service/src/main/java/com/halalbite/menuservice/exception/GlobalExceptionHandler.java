package com.halalbite.menuservice.exception;

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

    @ExceptionHandler(MenuExceptions.CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFound(
            MenuExceptions.CategoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, "Not Found", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MenuExceptions.ItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleItemNotFound(
            MenuExceptions.ItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, "Not Found", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MenuExceptions.UnauthorizedMenuAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            MenuExceptions.UnauthorizedMenuAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(403, "Forbidden", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MenuExceptions.DuplicateCategoryException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            MenuExceptions.DuplicateCategoryException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(409, "Conflict", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e -> {
            errors.put(((FieldError) e).getField(), e.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error in menu-service", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(500, "Internal Server Error",
                "An unexpected error occurred", LocalDateTime.now()));
    }
}
