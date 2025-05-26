package com.dengas.devtimetracker.exceptions;

import com.dengas.devtimetracker.dto.MyApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MyApiResponse<?>> handleException(Exception ex) {
        ex.printStackTrace(); // Логування (можна замінити на логер)
        return new ResponseEntity<>(new MyApiResponse<>(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<MyApiResponse<?>> handleUnauthorized(UnauthorizedException ex) {
        return new ResponseEntity<>(new MyApiResponse<>(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }
}
