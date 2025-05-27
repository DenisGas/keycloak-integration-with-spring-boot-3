package com.dengas.devtimetracker.controllers;

import com.dengas.devtimetracker.dto.CodeExchangeRequest;
import com.dengas.devtimetracker.dto.ErrorResponse;
import com.dengas.devtimetracker.dto.LoginRequest;
import com.dengas.devtimetracker.dto.TokenResponse;
import com.dengas.devtimetracker.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = {
        "http://localhost:5173",  // для разработки (если фронт запущен отдельно)
        "http://frontend:5173",   // для Docker-сети
        "http://127.0.0.1:5173"   // альтернативный localhost
})
@Tag(name = "Auth Controller", description = "Аутентификация через Keycloak")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Перенаправление на Keycloak для входа",
            description = "Возвращает редирект на страницу входа Keycloak для OAuth2 авторизации.",
            parameters = {
                    @Parameter(
                            name = "redirect_uri",
                            description = "URI для возврата пользователя после авторизации",
                            required = true,
                            example = "http://localhost:5173/callback"
                    ),
                    @Parameter(
                            name = "prompt",
                            description = "Принудительное показывание формы логина (true/false)",
                            required = false,
                            example = "false"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "Редирект на страницу авторизации Keycloak"
                    )
            }
    )
    @GetMapping("/login")
    public ResponseEntity<Void> login(
            @RequestParam String redirect_uri,
            @RequestParam(required = false, defaultValue = "false") boolean prompt) {
        String url = authService.buildLoginUrl(redirect_uri, prompt);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }

    @Operation(
            summary = "Логаут из Keycloak",
            description = "Выполняет выход пользователя из системы и перенаправляет на указанный URI.",
            parameters = {
                    @Parameter(
                            name = "redirect_uri",
                            description = "URI для редиректа после выхода",
                            required = true,
                            example = "http://localhost:5173/login"
                    ),
                    @Parameter(
                            name = "id_token_hint",
                            description = "ID токен пользователя для корректного выхода",
                            required = true,
                            example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "Редирект на страницу логаута Keycloak"
                    )
            }
    )
    @GetMapping("/logout")
    public void logout(
            @RequestParam String redirect_uri,
            @RequestParam String id_token_hint,
            HttpServletResponse response) throws IOException {
        String logoutUrl = authService.buildLogoutUrl(redirect_uri, id_token_hint);
        response.sendRedirect(logoutUrl);
    }

    @Operation(
            summary = "Обмен авторизационного кода на токены",
            description = "Обменивает полученный от Keycloak авторизационный код на JWT токены доступа.",
            requestBody = @RequestBody(
                    description = "Данные для обмена кода на токены",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CodeExchangeRequest.class),
                            examples = @ExampleObject(
                                    name = "Пример запроса",
                                    value = """
                                            {
                                              "code": "abc123def456",
                                              "redirectUri": "http://localhost:5173/callback"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешный обмен кода на токены",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TokenResponse.class),
                                    examples = @ExampleObject(
                                            name = "Успешный ответ",
                                            value = """
                                                    {
                                                      "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                      "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                      "expires_in": 3600
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Ошибка авторизации - неверный код",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            name = "Ошибка авторизации",
                                            value = """
                                                    {
                                                      "error": "Error exchanging code"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PostMapping("/code")
    public ResponseEntity<?> exchangeCode(@org.springframework.web.bind.annotation.RequestBody CodeExchangeRequest request) {
        return authService.exchangeCode(request.getCode(), request.getRedirectUri());
    }

    @Operation(
            summary = "Получение токенов по логину и паролю",
            description = "Аутентификация пользователя по имени пользователя и паролю (Resource Owner Password Credentials Grant).",
            requestBody = @RequestBody(
                    description = "Учетные данные пользователя",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "Пример запроса",
                                    value = """
                                            {
                                              "username": "user@example.com",
                                              "password": "password123"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешная аутентификация",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TokenResponse.class),
                                    examples = @ExampleObject(
                                            name = "Успешный ответ",
                                            value = """
                                                    {
                                                      "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                      "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                      "expires_in": 3600
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неверные учетные данные",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            name = "Ошибка аутентификации",
                                            value = """
                                                    {
                                                      "error": "Invalid username or password"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PostMapping("/token")
    public ResponseEntity<?> getTokenByCredentials(@org.springframework.web.bind.annotation.RequestBody LoginRequest credentials) {
        return authService.exchangeCredentials(credentials.getUsername(), credentials.getPassword());
    }
}