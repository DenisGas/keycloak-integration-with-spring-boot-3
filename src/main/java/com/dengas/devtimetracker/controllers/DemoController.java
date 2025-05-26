package com.dengas.devtimetracker.controllers;

import com.dengas.devtimetracker.dto.MessageResponse;
import com.dengas.devtimetracker.dto.MyApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/demo")
@Tag(name = "Demo Controller", description = "Перевірка ролей і доступу")
public class DemoController {

    @Operation(
            summary = "Привітання для client_user",
            description = "Повертає повідомлення, якщо у користувача є роль client_user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успішна відповідь",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MyApiResponse.class),
                                    examples = @ExampleObject(name = "ClientUserResponse", value = """
                                            {
                                              "data": {
                                                "message": "Hello from Spring Boot & Keycloak"
                                              },
                                              "error": null,
                                              "timestamp": "2025-05-25T23:20:11.206942600Z"
                                            }
                                            """)
                            )
                    ),
            }
    )
    @GetMapping
    @PreAuthorize("hasRole('client_user')")
    public ResponseEntity<MyApiResponse<MessageResponse>> hello() {
        return ResponseEntity.ok(new MyApiResponse<>(new MessageResponse("Hello from Spring Boot & Keycloak")));
    }

    @Operation(
            summary = "Привітання для client_admin",
            description = "Повертає повідомлення, якщо у користувача є роль client_admin",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успішна відповідь",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MyApiResponse.class),
                                    examples = @ExampleObject(name = "ClientAdminResponse", value = """
                                            {
                                              "data": {
                                                "message": "Hello from Spring Boot & Keycloak - ADMIN"
                                              },
                                              "error": null,
                                              "timestamp": "2025-05-25T23:20:11.206942600Z"
                                            }
                                            """)
                            )
                    ),
            }
    )
    @GetMapping("/hello-2")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<MyApiResponse<MessageResponse>> hello2() {
        return ResponseEntity.ok(new MyApiResponse<>(new MessageResponse("Hello from Spring Boot & Keycloak - ADMIN")));
    }

    @Operation(
            summary = "Привітання без авторизації",
            description = "Простий ендпоінт без перевірки ролей",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успішна відповідь",
                            content = @Content(
                                    mediaType = "text/plain",
                                    examples = @ExampleObject(name = "SimpleHelloResponse", value = "Hello")
                            )
                    )
            }
    )
    @GetMapping("/hello")
    public String hello3() {
        return "Hello";
    }
}
