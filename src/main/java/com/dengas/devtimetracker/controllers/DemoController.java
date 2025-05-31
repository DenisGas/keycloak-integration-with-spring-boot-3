package com.dengas.devtimetracker.controllers;

import com.dengas.devtimetracker.dto.MessageResponse;
import com.dengas.devtimetracker.dto.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo")
@Tag(name = "Demo Controller", description = "Перевірка ролей і доступу")
public class DemoController {

    @Operation(
            summary = "Привітання для client_user",
            description = "Повертає повідомлення, якщо у користувача є роль client_user",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успішна відповідь",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "ClientUserResponse",
                                            value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "message": "Hello from Spring Boot & Keycloak - USER"
                                              },
                                              "error": null,
                                              "timestamp": "2025-05-31T15:36:00.123456789",
                                              "status": 200
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неавторизовано (відсутній або прострочений токен)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "UnauthorizedResponse",
                                            value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "message": "Токен прострочений",
                                                "code": "TOKEN_EXPIRED"
                                              },
                                              "timestamp": "2025-05-31T15:36:00.123456789",
                                              "status": 401
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Доступ заборонено (немає ролі client_user)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "ForbiddenResponse",
                                            value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "message": "Доступ заборонено",
                                                "code": "FORBIDDEN"
                                              },
                                              "timestamp": "2025-05-31T15:36:00.123456789",
                                              "status": 403
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping
    @PreAuthorize("hasRole('client_user')")
    public ResponseEntity<ResponseWrapper<MessageResponse>> hello() {
        return ResponseEntity.ok(ResponseWrapper.success(new MessageResponse("Hello from Spring Boot & Keycloak - USER")));
    }

    @Operation(
            summary = "Привітання для client_admin",
            description = "Повертає повідомлення, якщо у користувача є роль client_admin",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успішна відповідь",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "ClientAdminResponse",
                                            value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "message": "Hello from Spring Boot & Keycloak - ADMIN"
                                              },
                                              "error": null,
                                              "timestamp": "2025-05-31T15:36:00.123456789",
                                              "status": 200
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неавторизовано (відсутній або прострочений токен)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "UnauthorizedResponse",
                                            value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "message": "Токен прострочений",
                                                "code": "TOKEN_EXPIRED"
                                              },
                                              "timestamp": "2025-05-31T15:36:00.123456789",
                                              "status": 401
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Доступ заборонено (немає ролі client_admin)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "ForbiddenResponse",
                                            value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "message": "Доступ заборонено",
                                                "code": "FORBIDDEN"
                                              },
                                              "timestamp": "2025-05-31T15:36:00.123456789",
                                              "status": 403
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/hello-2")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<ResponseWrapper<MessageResponse>> hello2() {
        return ResponseEntity.ok(ResponseWrapper.success(new MessageResponse("Hello from Spring Boot & Keycloak - ADMIN")));
    }

    @Operation(
            summary = "Привітання без авторизації",
            description = "Простий ендпоінт без перевірки ролей",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успішна відповідь",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "SimpleHelloResponse",
                                            value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "message": "Hello without authorisation"
                                              },
                                              "error": null,
                                              "timestamp": "2025-05-31T15:36:00.123456789",
                                              "status": 200
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/hello")
    public ResponseEntity<ResponseWrapper<MessageResponse>> hello3() {
        return ResponseEntity.ok(ResponseWrapper.success(new MessageResponse("Hello without authorisation")));
    }
}