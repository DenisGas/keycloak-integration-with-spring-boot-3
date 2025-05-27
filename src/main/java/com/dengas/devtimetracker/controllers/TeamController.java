package com.dengas.devtimetracker.controllers;

import com.dengas.devtimetracker.model.Team;
import com.dengas.devtimetracker.services.TeamService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "Team Management", description = "Endpoints for team management")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTeam(@RequestBody Team team) {
        return ResponseEntity.ok(teamService.createTeam(team));
    }

    @PostMapping("/{teamId}/members/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addMember(@PathVariable Long teamId, @PathVariable String userId) {
        return ResponseEntity.ok(teamService.addMember(teamId, userId));
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeMember(@PathVariable Long teamId, @PathVariable String userId) {
        teamService.removeMember(teamId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{teamId}/lead/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignTeamLead(@PathVariable Long teamId, @PathVariable String userId) {
        return ResponseEntity.ok(teamService.assignTeamLead(teamId, userId));
    }

    @GetMapping("/{teamId}/projects")
    @PreAuthorize("hasAnyRole('TEAM_LEAD', 'ADMIN')")
    public ResponseEntity<?> getTeamProjects(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTeamProjects(teamId));
    }
}