package com.dengas.devtimetracker.controllers;

import com.dengas.devtimetracker.dto.MyApiResponse;
import com.dengas.devtimetracker.exceptions.UnauthorizedException;
import com.dengas.devtimetracker.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = {
        "http://localhost:5173",  // для разработки (если фронт запущен отдельно)
        "http://frontend:5173",   // для Docker-сети
        "http://127.0.0.1:5173"   // альтернативный localhost
})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Отримати інформацію про користувача",
            description = "Повертає інформацію користувача",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успішне отримання",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = """
                                            {
                                              "data": {
                                                "name": "gasylo.dv",
                                                "email": "mishk908@gmail.com"
                                              },
                                              "error": null,
                                              "timestamp": "2025-05-26T13:45:00Z"
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неавторизовано",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = """
                                            {
                                              "data": null,
                                              "error": "Unauthorized",
                                              "timestamp": "2025-05-26T13:45:00Z"
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        var userInfo = userService.getUserInfo(jwt);
        return ResponseEntity.ok(new MyApiResponse<>(userInfo));
    }
    
    @PreAuthorize("hasRole('client_admin')")
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        var userInfo = userService.getUserInfoById(userId);
        return ResponseEntity.ok(new MyApiResponse<>(userInfo));
    }
}
