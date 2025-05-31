package com.dengas.devtimetracker.security;

import com.dengas.devtimetracker.dto.ResponseWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomBearerTokenAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final BearerTokenAuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // Делегування стандартної обробки для збереження заголовків, наприклад, WWW-Authenticate
        delegate.commence(request, response, authException);

        // Формуємо дружнє повідомлення про помилку
        String errorMessage = authException.getMessage();
        String errorCode = "UNAUTHORIZED";

        // Перевіряємо, чи помилка пов’язана з простроченим токеном
        if (errorMessage.contains("Jwt expired")) {
            errorMessage = "Токен прострочений";
            errorCode = "TOKEN_EXPIRED";
        } else if (errorMessage.contains("Invalid JWT")) {
            errorMessage = "Недійсний токен";
            errorCode = "INVALID_TOKEN";
        }

        ResponseWrapper<?> errorResponse = ResponseWrapper.error(
                HttpStatus.UNAUTHORIZED,
                errorMessage,
                errorCode
        );

        // Записуємо відповідь у форматі JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}