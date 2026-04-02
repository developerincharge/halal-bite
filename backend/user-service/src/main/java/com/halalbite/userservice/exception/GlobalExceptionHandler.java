package com.halalbite.userservice.exception;

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

/**
 * Custom Exceptions and Global Exception Handler
 *
 * Why a global exception handler?
 * Without it, Spring returns a generic 500 error with a stack trace —
 * which exposes your internals to the client and is hard to debug.
 *
 * With @RestControllerAdvice, every exception thrown anywhere in the
 * service is caught here and returned as a clean JSON response:
 * {
 *   "status": 404,
 *   "error": "User not found",
 *   "timestamp": "2026-04-01T10:00:00"
 * }
 */

// =====================================================
// CUSTOM EXCEPTION CLASSES
// =====================================================

class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}

class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}

class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

// =====================================================
// GLOBAL EXCEPTION HANDLER
// =====================================================

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Standard error response structure
    record ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {}

    // ---- 404 Not Found ----
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, "Not Found", ex.getMessage(), LocalDateTime.now()));
    }

    // ---- 409 Conflict — duplicate email ----
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailConflict(EmailAlreadyExistsException ex) {
        log.warn("Email conflict: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(409, "Conflict", ex.getMessage(), LocalDateTime.now()));
    }

    // ---- 401 Unauthorized ----
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse(401, "Unauthorized", ex.getMessage(), LocalDateTime.now()));
    }

    // ---- 400 Bad Request — validation failures ----
    // Triggered when @Valid fails on a request body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // ---- 500 Internal Server Error — catch-all ----
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        // Log the full stack trace for debugging — but don't send it to the client
        log.error("Unexpected error occurred", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(500, "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                LocalDateTime.now()));
    }
}
