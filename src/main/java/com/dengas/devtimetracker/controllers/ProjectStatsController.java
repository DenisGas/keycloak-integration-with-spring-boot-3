package com.dengas.devtimetracker.controllers;

import com.dengas.devtimetracker.model.ProjectStats;
import com.dengas.devtimetracker.services.ProjectStatsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Tag(name = "Project Statistics", description = "Endpoints for managing project statistics")
public class ProjectStatsController {

    private final ProjectStatsService projectStatsService;

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ProjectStats> getProjectStats(
            @PathVariable String projectId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(projectStatsService.getProjectStats(projectId, jwt));
    }

    @GetMapping("/projects")
    public ResponseEntity<?> getAllProjectStats(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(projectStatsService.getAllProjectStats(jwt));
    }
    
    // Новий ендпоінт для створення проекту
    @PostMapping("/projects")
    public ResponseEntity<?> createProject(
            @RequestBody ProjectStats stats,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(projectStatsService.createProject(stats, jwt));
    }

    // Змінений ендпоінт для оновлення проекту
    @PutMapping("/projects/{projectId}")
    public ResponseEntity<?> updateProjectStats(
            @PathVariable String projectId,
            @RequestBody ProjectStats stats,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(projectStatsService.updateProjectStats(projectId, stats, jwt));
    }

    @GetMapping("/projects/{projectId}/files")
    public ResponseEntity<?> getProjectFiles(@PathVariable String projectId) {
        return ResponseEntity.ok(projectStatsService.getProjectFiles(projectId));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        return ResponseEntity.ok(projectStatsService.getDashboardStats());
    }

    @GetMapping("/team")
    public ResponseEntity<?> getTeamMemberProjects() {
        return ResponseEntity.ok(projectStatsService.getTeamMemberProjects());
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<?> getProjectsByTeamId(@PathVariable Long teamId) {
        return ResponseEntity.ok(projectStatsService.getProjectsByTeamId(teamId));
    }
}