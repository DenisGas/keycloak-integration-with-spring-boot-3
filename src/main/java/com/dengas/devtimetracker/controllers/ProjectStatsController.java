package com.dengas.devtimetracker.controllers;

import com.dengas.devtimetracker.dto.ResponseWrapper;
import com.dengas.devtimetracker.model.ProjectStats;
import com.dengas.devtimetracker.model.FileStats;
import com.dengas.devtimetracker.services.ProjectStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Tag(name = "Project Statistics", description = "Endpoints for managing project statistics and analytics")
public class ProjectStatsController {

    private final ProjectStatsService projectStatsService;

    @Operation(
        summary = "Get project statistics by ID",
        description = "Retrieves detailed statistics for a specific project including daily stats and file information"
    )
    @ApiResponse(responseCode = "200", description = "Project statistics retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Project not found")
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ResponseWrapper<ProjectStats>> getProjectStats(
            @Parameter(description = "Project ID", required = true)
            @PathVariable String projectId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ResponseWrapper.success(projectStatsService.getProjectStats(projectId, jwt)));
    }

    @Operation(
        summary = "Get all projects statistics",
        description = "Retrieves statistics for all projects accessible to the current user"
    )
    @ApiResponse(responseCode = "200", description = "Projects list retrieved successfully")
    @GetMapping("/projects")
    public ResponseEntity<ResponseWrapper<List<ProjectStats>>> getAllProjectStats(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ResponseWrapper.success(projectStatsService.getAllProjectStats(jwt)));
    }

    @Operation(
            summary = "Створити новий проєкт",
            description = "Створює новий проєкт із початковою статистикою"
    )
    @ApiResponse(responseCode = "201", description = "Проєкт успішно створено")
    @ApiResponse(responseCode = "400", description = "Некоректні дані проєкту")
    @PostMapping("/projects")
    public ResponseEntity<ResponseWrapper<ProjectStats>> createProject(
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Дані проєкту для створення",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProjectStats.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Приклад створення проєкту",
                                    value = """
                                        {
                                            "projectPath": "/users/username/ппп/my-project",
                                            "githubBadgeVisible": true,
                                            "files": [
                                                {
                                                    "filePath": "/users/username/projects/my-project/src/main/java/App.java",
                                                    "type": "JAVA",
                                                    "dailyStats": {
                                                        "2024-01-20": {
                                                            "codingTime": 400,
                                                            "openTime": 800
                                                        },
                                                        "2024-01-21": {
                                                            "codingTime": 500,
                                                            "openTime": 1000
                                                        }
                                                    }
                                                },
                                                {
                                                    "filePath": "/users/username/projects/my-project/src/main/resources/application.properties",
                                                    "type": "PROPERTIES",
                                                    "dailyStats": {
                                                        "2024-01-20": {
                                                            "codingTime": 150,
                                                            "openTime": 300
                                                        },
                                                        "2024-01-21": {
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
            ProjectStats stats,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(projectStatsService.createProject(stats, jwt)));
    }

    @Operation(
        summary = "Update project statistics",
        description = "Updates existing project statistics with new data"
    )
    @ApiResponse(responseCode = "200", description = "Project updated successfully")
    @ApiResponse(responseCode = "404", description = "Project not found")
    @PutMapping("/projects/{projectId}")
    public ResponseEntity<ResponseWrapper<ProjectStats>> updateProjectStats(
            @Parameter(description = "Project ID", required = true)
            @PathVariable String projectId,
            @Valid @RequestBody ProjectStats stats,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ResponseWrapper.success(projectStatsService.updateProjectStats(projectId, stats, jwt)));
    }

    @Operation(
        summary = "Partially update project",
        description = "Updates specific fields of a project without affecting others"
    )
    @ApiResponse(responseCode = "200", description = "Project partially updated successfully")
    @ApiResponse(responseCode = "404", description = "Project not found")
    @PatchMapping("/projects/{projectId}")
    public ResponseEntity<ResponseWrapper<ProjectStats>> patchProjectStats(
            @Parameter(description = "Project ID", required = true)
            @PathVariable String projectId,
            @RequestBody ProjectStats stats,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ResponseWrapper.success(projectStatsService.patchProjectStats(projectId, stats, jwt)));
    }

    @Operation(
        summary = "Delete project",
        description = "Deletes a project and all associated statistics"
    )
    @ApiResponse(responseCode = "204", description = "Project deleted successfully")
    @ApiResponse(responseCode = "404", description = "Project not found")
    @DeleteMapping("/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "Project ID", required = true)
            @PathVariable String projectId,
            @AuthenticationPrincipal Jwt jwt) {
        projectStatsService.deleteProject(projectId, jwt);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get project files",
        description = "Retrieves statistics for all files in a specific project"
    )
    @ApiResponse(responseCode = "200", description = "Project files retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Project not found")
    @GetMapping("/projects/{projectId}/files")
    public ResponseEntity<ResponseWrapper<List<FileStats>>> getProjectFiles(
            @Parameter(description = "Project ID", required = true)
            @PathVariable String projectId) {
        return ResponseEntity.ok(ResponseWrapper.success(projectStatsService.getProjectFiles(projectId)));
    }

    @Operation(
        summary = "Get dashboard statistics",
        description = "Retrieves aggregated statistics for dashboard display"
    )
    @ApiResponse(responseCode = "200", description = "Dashboard statistics retrieved successfully")
    @GetMapping("/dashboard")
    public ResponseEntity<ResponseWrapper<Object>> getDashboardStats() {
        return ResponseEntity.ok(ResponseWrapper.success(projectStatsService.getDashboardStats()));
    }

    @Operation(
        summary = "Get team member projects",
        description = "Retrieves projects statistics for all team members"
    )
    @ApiResponse(responseCode = "200", description = "Team projects retrieved successfully")
    @GetMapping("/team")
    public ResponseEntity<ResponseWrapper<Object>> getTeamMemberProjects() {
        return ResponseEntity.ok(ResponseWrapper.success(projectStatsService.getTeamMemberProjects()));
    }

    @Operation(
        summary = "Get projects by team ID",
        description = "Retrieves all projects statistics for a specific team"
    )
    @ApiResponse(responseCode = "200", description = "Team projects retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Team not found")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<ResponseWrapper<Object>> getProjectsByTeamId(
            @Parameter(description = "Team ID", required = true)
            @PathVariable Long teamId) {
        return ResponseEntity.ok(ResponseWrapper.success(projectStatsService.getProjectsByTeamId(teamId)));
    }

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