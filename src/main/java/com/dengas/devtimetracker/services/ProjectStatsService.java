package com.dengas.devtimetracker.services;

import com.dengas.devtimetracker.dto.ResponseWrapper;
import com.dengas.devtimetracker.model.FileStats;
import com.dengas.devtimetracker.model.ProjectStats;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

public interface ProjectStatsService {
    ResponseWrapper<List<ProjectStats>> getAllProjectStats(Jwt jwt);
    ResponseWrapper<ProjectStats> getProjectStats(String projectId, Jwt jwt);
    ResponseWrapper<ProjectStats> createProject(ProjectStats stats, Jwt jwt);
    ResponseWrapper<ProjectStats> updateProjectStats(String projectId, ProjectStats stats, Jwt jwt);
    ResponseWrapper<ProjectStats> patchProjectStats(String projectId, ProjectStats stats, Jwt jwt);
    ResponseWrapper<String> deleteProject(String projectId, Jwt jwt);
    ResponseWrapper<List<FileStats>> getProjectFiles(String projectId);
    ResponseWrapper<Map<String, Object>> getDashboardStats(Jwt jwt);
//    ResponseWrapper<List<Map<String, Object>>> getTeamMemberProjects(Jwt jwt);
//    ResponseWrapper<List<ProjectStats>> getProjectsByTeamId(Long teamId, Jwt jwt);
    String generateBadge(String label, String value, String color);
    ProjectStats findProjectById(String projectId);
}