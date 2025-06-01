package com.dengas.devtimetracker.controllers;

import com.dengas.devtimetracker.dto.ResponseWrapper;
import com.dengas.devtimetracker.model.ProjectStats;
import com.dengas.devtimetracker.model.FileStats;
import com.dengas.devtimetracker.services.ProjectStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stats")
@Tag(name = "Project Statistics", description = "Endpoints for managing project statistics and analytics")
public class ProjectStatsController {

    private final ProjectStatsService projectStatsService;

    public ProjectStatsController(ProjectStatsService projectStatsService) {
        this.projectStatsService = projectStatsService;
    }

    @Operation(
            summary = "Get project statistics by ID",
            description = "Retrieves detailed statistics for a specific project, including daily stats and file information.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Project statistics retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "SuccessResponse",
                                            value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "projectId": "123e4567-e89b-12d3-a456-426614174000",
                                                "projectPath": "/users/username/projects/my-project",
                                                "githubBadgeVisible": true,
                                                "totalCodingTime": 3600,
                                                "totalOpenTime": 7200,
                                                "dailyStats": {
                                                  "2025-05-30": {
                                                    "codingTime": 1800,
                                                    "openTime": 3600
                                                  }
                                                },
                                                "files": [
                                                  {
                                                    "filePath": "/users/username/projects/my-project/src/main/java/App.java",
                                                    "type": "JAVA",
                                                    "dailyStats": {
                                                      "2025-05-30": {
                                                        "codingTime": 1800,
                                                        "openTime": 3600
                                                      }
                                                    }
                                                  }
                                                ]
                                              },
                                              "error": null,
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 200
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Unauthorized access to the project",
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
                                                "message": "You do not have access to this project",
                                                "code": "UNAUTHORIZED"
                                              },
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 403
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Project not found",
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
                                                "message": "Project not found with ID: 123e4567-e89b-12d3-a456-426614174000",
                                                "code": "NOT_FOUND"
                                              },
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 404
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/projects/{projectId}")
    @PreAuthorize("hasRole('client_user')")
    public ResponseEntity<ResponseWrapper<ProjectStats>> getProjectStats(
            @Parameter(description = "Project ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String projectId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(projectStatsService.getProjectStats(projectId, jwt));
    }

    @Operation(
            summary = "Get all project statistics",
            description = "Retrieves statistics for all projects accessible to the authenticated user. Admins can access all projects.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Projects list retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "SuccessResponse",
                                            value = """
                                            {
                                              "success": true,
                                              "data": [
                                                {
                                                  "projectId": "123e4567-e89b-12d3-a456-426614174000",
                                                  "projectPath": "/users/username/projects/my-project",
                                                  "githubBadgeVisible": true,
                                                  "totalCodingTime": 3600,
                                                  "totalOpenTime": 7200,
                                                  "dailyStats": {
                                                    "2025-05-30": {
                                                      "codingTime": 1800,
                                                      "openTime": 3600
                                                    }
                                                  },
                                                  "files": []
                                                }
                                              ],
                                              "error": null,
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 200
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Invalid or missing JWT token",
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
                                                "message": "Invalid or missing JWT token",
                                                "code": "UNAUTHORIZED"
                                              },
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 403
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/projects")
    @PreAuthorize("hasRole('client_user')")
    public ResponseEntity<ResponseWrapper<List<ProjectStats>>> getAllProjectStats(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(projectStatsService.getAllProjectStats(jwt));
    }

    @Operation(
            summary = "Create a new project",
            description = "Creates a new project with initial statistics for the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Project created successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "SuccessResponse",
                                            value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "projectId": "123e4567-e89b-12d3-a456-426614174000",
                                                "projectPath": "/users/username/projects/my-project",
                                                "githubBadgeVisible": false,
                                                "totalCodingTime": 550,
                                                "totalOpenTime": 1100,
                                                "dailyStats": {
                                                  "2025-05-30": {
                                                    "codingTime": 550,
                                                    "openTime": 1100
                                                  }
                                                },
                                                "files": [
                                                  {
                                                    "filePath": "/users/username/projects/my-project/src/main/java/App.java",
                                                    "type": "JAVA",
                                                    "dailyStats": {
                                                      "2025-05-30": {
                                                        "codingTime": 400,
                                                        "openTime": 800
                                                      }
                                                    }
                                                  }
                                                ]
                                              },
                                              "error": null,
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 201
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid project data",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "BadRequestResponse",
                                            value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "message": "File path is required",
                                                "code": "VALIDATION_ERROR"
                                              },
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 400
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Invalid or missing JWT token",
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
                                                "message": "Invalid or missing JWT token",
                                                "code": "UNAUTHORIZED"
                                              },
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 403
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @PostMapping("/projects")
    @PreAuthorize("hasRole('client_user')")
    public ResponseEntity<ResponseWrapper<ProjectStats>> createProject(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Project data to create",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProjectStats.class),
                            examples = @ExampleObject(
                                    name = "CreateProjectRequest",
                                    value = """
                                    {
                                      "projectPath": "/users/username/projects/my-project",
                                      "githubBadgeVisible": true,
                                      "files": [
                                        {
                                          "filePath": "/users/username/projects/my-project/src/main/java/App.java",
                                          "type": "JAVA",
                                          "dailyStats": {
                                            "2025-05-30": {
                                              "codingTime": 400,
                                              "openTime": 800
                                            }
                                          }
                                        },
                                        {
                                          "filePath": "/users/username/projects/my-project/src/main/resources/application.properties",
                                          "type": "PROPERTIES",
                                          "dailyStats": {
                                            "2025-05-30": {
                                              "codingTime": 150,
                                              "openTime": 300
                                            }
                                          }
                                        }
                                      ]
                                    }
                                    """
                            )
                    )
            )
            @RequestBody ProjectStats stats,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(projectStatsService.createProject(stats, jwt));
    }

    @Operation(
            summary = "Update project statistics",
            description = "Updates an existing project's statistics with new data, replacing all fields.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Project updated successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "SuccessResponse",
                                            value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "projectId": "123e4567-e89b-12d3-a456-426614174000",
                                                "projectPath": "/users/username/projects/my-project-updated",
                                                "githubBadgeVisible": true,
                                                "totalCodingTime": 3600,
                                                "totalOpenTime": 7200,
                                                "dailyStats": {
                                                  "2025-05-30": {
                                                    "codingTime": 1800,
                                                    "openTime": 3600
                                                  }
                                                },
                                                "files": []
                                              },
                                              "error": null,
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 200
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid project data",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "BadRequestResponse",
                                            value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "message": "File path is required",
                                                "code": "VALIDATION_ERROR"
                                              },
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 400
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Unauthorized access to the project",
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
                                                "message": "You do not have access to this project",
                                                "code": "UNAUTHORIZED"
                                              },
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 403
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Project not found",
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
                                                "message": "Project not found with ID: 123e4567-e89b-12d3-a456-426614174000",
                                                "code": "NOT_FOUND"
                                              },
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 404
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @PutMapping("/projects/{projectId}")
    @PreAuthorize("hasRole('client_user')")
    public ResponseEntity<ResponseWrapper<ProjectStats>> updateProjectStats(
            @Parameter(description = "Project ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String projectId,
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated project data",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProjectStats.class),
                            examples = @ExampleObject(
                                    name = "UpdateProjectRequest",
                                    value = """
                                    {
                                      "projectPath": "/users/username/projects/my-project-updated",
                                      "githubBadgeVisible": true,
                                      "files": [
                                        {
                                          "filePath": "/users/username/projects/my-project/src/main/java/App.java",
                                          "type": "JAVA",
                                          "dailyStats": {
                                            "2025-05-30": {
                                              "codingTime": 1800,
                                              "openTime": 3600
                                            }
                                          }
                                        }
                                      ]
                                    }
                                    """
                            )
                    )
            )
            @RequestBody ProjectStats stats,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(projectStatsService.updateProjectStats(projectId, stats, jwt));
    }

    @Operation(
            summary = "Partially update project",
            description = "Updates specific fields of a project without affecting others.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Project partially updated successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "SuccessResponse",
                                            value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "projectId": "123e4567-e89b-12d3-a456-426614174000",
                                                "projectPath": "/users/username/projects/my-project",
                                                "githubBadgeVisible": true,
                                                "totalCodingTime": 3600,
                                                "totalOpenTime": 7200,
                                                "dailyStats": {
                                                  "2025-05-30": {
                                                    "codingTime": 1800,
                                                    "openTime": 3600
                                                  }
                                                },
                                                "files": []
                                              },
                                              "error": null,
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 200
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid project data",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "BadRequestResponse",
                                            value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "message": "File path is required",
                                                "code": "VALIDATION_ERROR"
                                              },
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 400
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Unauthorized access to the project",
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
                                                "message": "You do not have access to this project",
                                                "code": "UNAUTHORIZED"
                                              },
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 403
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Project not found",
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
                                                "message": "Project not found with ID: 123e4567-e89b-12d3-a456-426614174000",
                                                "code": "NOT_FOUND"
                                              },
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 404
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @PatchMapping("/projects/{projectId}")
    @PreAuthorize("hasRole('client_user')")
    public ResponseEntity<ResponseWrapper<ProjectStats>> patchProjectStats(
            @Parameter(description = "Project ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String projectId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Partial project data to update",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProjectStats.class),
                            examples = @ExampleObject(
                                    name = "PatchProjectRequest",
                                    value = """
                                    {
                                      "githubBadgeVisible": true
                                    }
                                    """
                            )
                    )
            )
            @RequestBody ProjectStats stats,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(projectStatsService.patchProjectStats(projectId, stats, jwt));
    }

    @Operation(
            summary = "Delete a project",
            description = "Deletes a project and all associated statistics for the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Project deleted successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "SuccessResponse",
                                            value = """
                                            {
                                              "success": true,
                                              "data": "Project deleted successfully",
                                              "error": null,
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 200
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Unauthorized access to the project",
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
                                                "message": "You do not have access to this project",
                                                "code": "UNAUTHORIZED"
                                              },
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 403
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Project not found",
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
                                                "message": "Project not found with ID: 123e4567-e89b-12d3-a456-426614174000",
                                                "code": "NOT_FOUND"
                                              },
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 404
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @DeleteMapping("/projects/{projectId}")
    @PreAuthorize("hasRole('client_user')")
    public ResponseEntity<ResponseWrapper<String>> deleteProject(
            @Parameter(description = "Project ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String projectId,
            @AuthenticationPrincipal Jwt jwt) {
        ResponseWrapper<String> response = projectStatsService.deleteProject(projectId, jwt);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(
            summary = "Get project files",
            description = "Retrieves statistics for all files in a specific project.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Project files retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "SuccessResponse",
                                            value = """
                                            {
                                              "success": true,
                                              "data": [
                                                {
                                                  "filePath": "/users/username/projects/my-project/src/main/java/App.java",
                                                  "type": "JAVA",
                                                  "dailyStats": {
                                                    "2025-05-30": {
                                                      "codingTime": 1800,
                                                      "openTime": 3600
                                                    }
                                                  }
                                                }
                                              ],
                                              "error": null,
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 200
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Project not found",
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
                                                "message": "Project not found with ID: 123e4567-e89b-12d3-a456-426614174000",
                                                "code": "NOT_FOUND"
                                              },
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 404
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/projects/{projectId}/files")
    @PreAuthorize("hasRole('client_user')")
    public ResponseEntity<ResponseWrapper<List<FileStats>>> getProjectFiles(
            @Parameter(description = "Project ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String projectId) {
        return ResponseEntity.ok(projectStatsService.getProjectFiles(projectId));
    }

    @Operation(
            summary = "Get dashboard statistics",
            description = "Retrieves aggregated statistics for the dashboard, such as total coding time and project count.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Dashboard statistics retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResponseWrapper.class),
                                    examples = @ExampleObject(
                                            name = "SuccessResponse",
                                            value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "totalProjects": 5,
                                                "totalCodingTime": 18000,
                                                "totalOpenTime": 36000,
                                                "averageCodingTimePerProject": 3600
                                              },
                                              "error": null,
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 200
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Invalid or missing JWT token",
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
                                                "message": "Invalid or missing JWT token",
                                                "code": "UNAUTHORIZED"
                                              },
                                              "timestamp": "2025-05-31T16:14:00.123456789",
                                              "status": 403
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('client_user')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getDashboardStats(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(projectStatsService.getDashboardStats(jwt));
    }

//    @Operation(
//            summary = "Get team member projects",
//            description = "Retrieves project statistics for all team members associated with the authenticated user's team.",
//            security = @SecurityRequirement(name = "bearerAuth"),
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "Team projects retrieved successfully",
//                            content = @Content(
//                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
//                                    schema = @Schema(implementation = ResponseWrapper.class),
//                                    examples = @ExampleObject(
//                                            name = "SuccessResponse",
//                                            value = """
//                                            {
//                                              "success": true,
//                                              "data": [
//                                                {
//                                                  "userId": "user123",
//                                                  "projects": [
//                                                    {
//                                                      "projectId": "123e4567-e89b-12d3-a456-426614174000",
//                                                      "projectPath": "/users/username/projects/my-project",
//                                                      "totalCodingTime": 3600
//                                                    }
//                                                  ]
//                                                }
//                                              ],
//                                              "error": null,
//                                              "timestamp": "2025-05-31T16:14:00.123456789",
//                                              "status": 200
//                                            }
//                                            """
//                                    )
//                            )
//                    ),
//                    @ApiResponse(
//                            responseCode = "403",
//                            description = "Invalid or missing JWT token",
//                            content = @Content(
//                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
//                                    schema = @Schema(implementation = ResponseWrapper.class),
//                                    examples = @ExampleObject(
//                                            name = "UnauthorizedResponse",
//                                            value = """
//                                            {
//                                              "success": false,
//                                              "data": null,
//                                              "error": {
//                                                "message": "Invalid or missing JWT token",
//                                                "code": "UNAUTHORIZED"
//                                              },
//                                              "timestamp": "2025-05-31T16:14:00.123456789",
//                                              "status": 403
//                                            }
//                                            """
//                                    )
//                            )
//                    )
//            }
//    )
//    @GetMapping("/team")
//    public ResponseEntity<ResponseWrapper<List<Map<String, Object>>>> getTeamMemberProjects(@AuthenticationPrincipal Jwt jwt) {
//        return ResponseEntity.ok(projectStatsService.getTeamMemberProjects(jwt));
//    }
//
//    @Operation(
//            summary = "Get projects by team ID",
//            description = "Retrieves all project statistics for a specific team identified by team ID.",
//            security = @SecurityRequirement(name = "bearerAuth"),
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "Team projects retrieved successfully",
//                            content = @Content(
//                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
//                                    schema = @Schema(implementation = ResponseWrapper.class),
//                                    examples = @ExampleObject(
//                                            name = "SuccessResponse",
//                                            value = """
//                                            {
//                                              "success": true,
//                                              "data": [
//                                                {
//                                                  "projectId": "123e4567-e89b-12d3-a456-426614174000",
//                                                  "projectPath": "/users/username/projects/my-project",
//                                                  "totalCodingTime": 3600,
//                                                  "totalOpenTime": 7200
//                                                }
//                                              ],
//                                              "error": null,
//                                              "timestamp": "2025-05-31T16:14:00.123456789",
//                                              "status": 200
//                                            }
//                                            """
//                                    )
//                            )
//                    ),
//                    @ApiResponse(
//                            responseCode = "403",
//                            description = "Invalid or missing JWT token",
//                            content = @Content(
//                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
//                                    schema = @Schema(implementation = ResponseWrapper.class),
//                                    examples = @ExampleObject(
//                                            name = "UnauthorizedResponse",
//                                            value = """
//                                            {
//                                              "success": false,
//                                              "data": null,
//                                              "error": {
//                                                "message": "Invalid or missing JWT token",
//                                                "code": "UNAUTHORIZED"
//                                              },
//                                              "timestamp": "2025-05-31T16:14:00.123456789",
//                                              "status": 403
//                                            }
//                                            """
//                                    )
//                            )
//                    ),
//                    @ApiResponse(
//                            responseCode = "404",
//                            description = "Team not found",
//                            content = @Content(
//                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
//                                    schema = @Schema(implementation = ResponseWrapper.class),
//                                    examples = @ExampleObject(
//                                            name = "NotFoundResponse",
//                                            value = """
//                                            {
//                                              "success": false,
//                                              "data": null,
//                                              "error": {
//                                                "message": "Team not found with ID: 1",
//                                                "code": "NOT_FOUND"
//                                              },
//                                              "timestamp": "2025-05-31T16:14:00.123456789",
//                                              "status": 404
//                                            }
//                                            """
//                                    )
//                            )
//                    )
//            }
//    )
//    @GetMapping("/team/{teamId}")
//    public ResponseEntity<ResponseWrapper<List<ProjectStats>>> getProjectsByTeamId(
//            @Parameter(description = "Team ID", required = true, example = "1")
//            @PathVariable Long teamId,
//            @AuthenticationPrincipal Jwt jwt) {
//        return ResponseEntity.ok(projectStatsService.getProjectsByTeamId(teamId, jwt));
//    }

    @Operation(
            summary = "Generate project badge",
            description = "Generates an SVG badge displaying the total coding time for a project, if the badge is visible.",
            parameters = {
                    @Parameter(name = "projectId", description = "Project ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000"),
                    @Parameter(name = "color", description = "Badge color in hex format", required = false, example = "#4c1")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Badge generated successfully",
                            content = @Content(
                                    mediaType = "MediaType.IMAGE_SVG_XML_VALUE",
                                    schema = @Schema(type = "string", format = "binary"),
                                    examples = @ExampleObject(
                                            name = "SuccessResponse",
                                            value = """
                                        <svg xmlns="http://www.w3.org/2000/svg" width="146" height="20">
                                          <linearGradient id="b" x2="0" y2="100%">
                                            <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
                                            <stop offset="1" stop-opacity=".1"/>
                                          </linearGradient>
                                          <mask id="a">
                                            <rect width="146" height="20" rx="3" fill="#fff"/>
                                          </mask>
                                          <g mask="url(#a)">
                                            <rect width="85" height="20" fill="#555"/>
                                            <rect x="85" width="61" height="20" fill="#4c1"/>
                                            <rect width="146" height="20" fill="url(#b)"/>
                                          </g>
                                          <g fill="#fff" text-anchor="middle" font-family="Verdana,Geneva,DejaVu Sans,sans-serif" font-size="11">
                                            <text x="42.5" y="15" fill="#010101" fill-opacity=".3">Coding time</text>
                                            <text x="42.5" y="14">Coding time</text>
                                            <text x="115.5" y="15" fill="#010101" fill-opacity=".3">1h 0min</text>
                                            <text x="115.5" y="14">1h 0min</text>
                                          </g>
                                        </svg>
                                        """
                                    )
                            )
                    ),
            }
    )
    @GetMapping(path = "/badge", produces = "image/svg+xml")
    public ResponseEntity<String> generateBadge(
            @RequestParam String projectId,
            @RequestParam(defaultValue = "#4c1") String color
    ) {
        ProjectStats project = projectStatsService.findProjectById(projectId);

        if (project == null || !Boolean.TRUE.equals(project.isGithubBadgeVisible())) {
            String badge = projectStatsService.generateBadge("Coding time", "Not Found", "#red");
            return ResponseEntity.ok(badge);
        }

        // Конвертація загального часу з секунд в формат "Xh Ymin"
        long totalSeconds = project.getTotalCodingTime(); // Наприклад: 3660
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        String value = String.format("%dh %dmin", hours, minutes);

        String badge = projectStatsService.generateBadge("Coding time", value, color);
        return ResponseEntity.ok(badge);
    }
}