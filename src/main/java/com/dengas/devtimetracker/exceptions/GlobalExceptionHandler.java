package com.dengas.devtimetracker.exceptions;

import com.dengas.devtimetracker.dto.ResponseWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseWrapper<?>> handleAuthenticationException(AuthenticationException ex) {
        String errorMessage = ex.getMessage();
        String errorCode = "UNAUTHORIZED";

        if (errorMessage.contains("Jwt expired")) {
            errorMessage = "Токен прострочений";
            errorCode = "TOKEN_EXPIRED";
        } else if (errorMessage.contains("Invalid JWT")) {
            errorMessage = "Недійсний токен";
            errorCode = "INVALID_TOKEN";
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ResponseWrapper.error(
                        HttpStatus.UNAUTHORIZED,
                        errorMessage,
                        errorCode
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseWrapper<?>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseWrapper.error(
                        HttpStatus.FORBIDDEN,
                        "Доступ заборонено",
                        "FORBIDDEN"
                ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseWrapper<?>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapper.error(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage(),
                        "USER_NOT_FOUND"
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<?>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        String message = "Помилка валідації: " +
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
                        "Невірний тип параметра",
                        "INVALID_PARAMETER_TYPE"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<?>> handleException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Внутрішня помилка сервера",
                        "INTERNAL_ERROR"
                ));
    }
}