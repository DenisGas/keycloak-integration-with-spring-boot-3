package com.dengas.devtimetracker.services;

import com.dengas.devtimetracker.model.ProjectStats;
import com.dengas.devtimetracker.model.FileStats;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface ProjectStatsService {
    ProjectStats getProjectStats(String projectId, Jwt jwt);
    List<ProjectStats> getAllProjectStats(Jwt jwt);
    ProjectStats createProject(ProjectStats stats, Jwt jwt);
    ProjectStats updateProjectStats(String projectId, ProjectStats stats, Jwt jwt);
    List<FileStats> getProjectFiles(String projectId);
    Object getDashboardStats();

    Object getTeamMemberProjects();

    Object getProjectsByTeamId(Long teamId);
}