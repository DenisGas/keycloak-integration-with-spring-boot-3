package com.dengas.devtimetracker.exceptions;

import com.dengas.devtimetracker.dto.ResponseWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ResponseWrapper<?>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ResponseWrapper.error(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                "UNAUTHORIZED"
            ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<?>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        
        String message = "Validation failed: " + 
            errors.entrySet().stream()
                .map(e -> e.getKey() + " " + e.getValue())
                .collect(Collectors.joining(", "));
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ResponseWrapper.error(
                HttpStatus.BAD_REQUEST,
                message,
                "VALIDATION_ERROR"
            ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseWrapper<?>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ResponseWrapper.error(
                HttpStatus.BAD_REQUEST,
                "Invalid parameter type",
                "INVALID_PARAMETER_TYPE"
            ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<?>> handleException(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ResponseWrapper.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "INTERNAL_ERROR"
            ));
    }
}
