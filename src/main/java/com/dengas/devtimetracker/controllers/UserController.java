package com.dengas.devtimetracker.controllers;

import com.dengas.devtimetracker.dto.ResponseWrapper;
import com.dengas.devtimetracker.exceptions.ResourceNotFoundException;
import com.dengas.devtimetracker.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://frontend:5173",
        "http://127.0.0.1:5173"
})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Отримати інформацію про поточного користувача",
            description = "Повертає інформацію про поточного аутентифікованого користувача на основі JWT-токена.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успішне отримання інформації про користувача",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "SuccessResponse",
                                            value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "id": "12345",
                                                "email": "mishk908@gmail.com",
                                                "name": "gasylo.dv",
                                                "createdAt": "2025-05-31T14:01:19Z",
                                                "updatedAt": "2025-05-31T14:01:19Z",
                                                "isTeamLead": false,
                                                "teamIds": ["team1", "team2"]
                                              },
                                              "timestamp": "2025-05-31T14:01:19.7352919",
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
                                              "timestamp": "2025-05-31T14:01:19.7352919",
                                              "status": 401
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/me")
    public ResponseEntity<ResponseWrapper<?>> getCurrentUser(
            @AuthenticationPrincipal
            @Parameter(description = "JWT-токен поточного користувача", hidden = true)
            Jwt jwt) {
        var userInfo = userService.getUserInfo(jwt);
        return ResponseEntity.ok(ResponseWrapper.success(userInfo));
    }

    @Operation(
            summary = "Отримати інформацію про користувача за ID",
            description = "Повертає інформацію про користувача за його ідентифікатором. Доступно тільки для користувачів із роллю 'client_admin'.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успішне отримання інформації про користувача",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "SuccessResponse",
                                            value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "id": "12345",
                                                "email": "mishk908@gmail.com",
                                                "name": "gasylo.dv",
                                                "createdAt": "2025-05-31T14:01:19Z",
                                                "updatedAt": "2025-05-31T14:01:19Z",
                                                "isTeamLead": true,
                                                "teamIds": ["team1", "team2"],
                                                "leadingTeamId": "team1"
                                              },
                                              "timestamp": "2025-05-31T14:01:19.7352919",
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
                                              "timestamp": "2025-05-31T14:01:19.7352919",
                                              "status": 401
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Доступ заборонено (недостатньо прав)",
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
                                              "timestamp": "2025-05-31T14:01:19.7352919",
                                              "status": 403
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Користувача не знайдено",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "NotFoundResponse",
                                            value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "message": "Користувача не знайдено",
                                                "code": "USER_NOT_FOUND"
                                              },
                                              "timestamp": "2025-05-31T14:01:19.7352919",
                                              "status": 404
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('client_admin')")
    @GetMapping("/{userId}")
    public ResponseEntity<ResponseWrapper<?>> getUserById(
            @PathVariable
            @Parameter(description = "Ідентифікатор користувача", example = "653543ec-4ef9-4b07-bae1-7fadaac5687f")
            String userId) {
        try {
            var userInfo = userService.getUserInfoById(userId);
            return ResponseEntity.ok(ResponseWrapper.success(userInfo));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseWrapper.error(HttpStatus.NOT_FOUND, "Користувача не знайдено", "USER_NOT_FOUND"));
        }
    }
}