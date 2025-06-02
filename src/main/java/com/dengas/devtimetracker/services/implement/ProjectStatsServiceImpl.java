package com.dengas.devtimetracker.services.implement;

import com.dengas.devtimetracker.dto.ResponseWrapper;
import com.dengas.devtimetracker.exceptions.ResourceNotFoundException;
import com.dengas.devtimetracker.exceptions.UnauthorizedException;
import com.dengas.devtimetracker.factory.UserFactory;
import com.dengas.devtimetracker.model.DailyStats;
import com.dengas.devtimetracker.model.FileStats;
import com.dengas.devtimetracker.model.ProjectStats;
import com.dengas.devtimetracker.model.User;
import com.dengas.devtimetracker.repositories.FileStatsRepository;
import com.dengas.devtimetracker.repositories.ProjectStatsRepository;
import com.dengas.devtimetracker.repositories.UserRepository;
import com.dengas.devtimetracker.services.ProjectStatsService;
import com.dengas.devtimetracker.utils.SecurityUtils;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class ProjectStatsServiceImpl implements ProjectStatsService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectStatsServiceImpl.class);

    private final ProjectStatsRepository projectStatsRepository;
    private final FileStatsRepository fileStatsRepository;
    private final UserRepository userRepository;
    private final UserFactory userFactory;

    public ProjectStatsServiceImpl(ProjectStatsRepository projectStatsRepository,
                                   FileStatsRepository fileStatsRepository,
                                   UserRepository userRepository,
                                   UserFactory userFactory) {
        this.projectStatsRepository = projectStatsRepository;
        this.fileStatsRepository = fileStatsRepository;
        this.userRepository = userRepository;
        this.userFactory = userFactory;
    }

    @Override
    public ResponseWrapper<List<ProjectStats>> getAllProjectStats(Jwt jwt) {
        try {
            List<ProjectStats> projects;
            if (SecurityUtils.isAdmin(jwt)) {
                projects = projectStatsRepository.findAll();
            } else {
                String userId = jwt.getSubject();
                projects = projectStatsRepository.findByUserId(userId);
            }

            for (ProjectStats project : projects) {
                List<FileStats> files = fileStatsRepository.findByProjectId(project.getProjectId());
                project.setFiles(files);
            }

            return ResponseWrapper.success(projects);
        } catch (Exception e) {
            logger.error("Error retrieving all project stats: {}", e.getMessage(), e);
            return ResponseWrapper.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve project stats", "INTERNAL_ERROR");
        }
    }

    @Override
    public ResponseWrapper<ProjectStats> getProjectStats(String projectId, Jwt jwt) {
        try {
            String userId = jwt.getSubject();
            ProjectStats project = projectStatsRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

            if (project.getUser() != null && !project.getUser().getId().equals(userId) && !SecurityUtils.isAdmin(jwt)) {
                throw new UnauthorizedException("You do not have access to this project");
            }

            List<FileStats> files = fileStatsRepository.findByProjectId(projectId);
            project.setFiles(files);

            return ResponseWrapper.success(project);
        } catch (ResourceNotFoundException e) {
            logger.error("Project not found: {}", e.getMessage());
            return ResponseWrapper.error(HttpStatus.NOT_FOUND, e.getMessage(), "NOT_FOUND");
        } catch (UnauthorizedException e) {
            logger.error("Unauthorized access: {}", e.getMessage());
            return ResponseWrapper.error(HttpStatus.FORBIDDEN, e.getMessage(), "UNAUTHORIZED");
        } catch (Exception e) {
            logger.error("Error retrieving project stats: {}", e.getMessage(), e);
            return ResponseWrapper.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve project stats", "INTERNAL_ERROR");
        }
    }

    @Override
    @Transactional
    public ResponseWrapper<ProjectStats> createProject(ProjectStats stats, Jwt jwt) {
        try {
            String projectId = UUID.randomUUID().toString();
            stats.setProjectId(projectId);
            stats.setGithubBadgeVisible(stats.isGithubBadgeVisible());

            String userId = jwt.getSubject();
            User user = userRepository.findById(userId)
                    .map(existingUser -> {
                        User updatedUser = userFactory.updateIfNeeded(existingUser);
                        return updatedUser != null ? userRepository.save(updatedUser) : existingUser;
                    })
                    .orElseGet(() -> {
                        User newUser = userFactory.createFromJwt(jwt);
                        return userRepository.save(newUser);
                    });

            stats.setUser(user);

            ProjectStats savedProject = projectStatsRepository.save(stats);

            List<FileStats> savedFiles = new ArrayList<>();
            if (stats.getFiles() != null && !stats.getFiles().isEmpty()) {
                for (FileStats file : stats.getFiles()) {
                    validateFileStats(file);
                    file.setProjectId(savedProject.getProjectId());

                    if (file.getDailyStats() != null && !file.getDailyStats().isEmpty()) {
                        file.calculateTotalTimes();
                    }

                    FileStats savedFile = fileStatsRepository.save(file);
                    savedFiles.add(savedFile);
                }
            }

            calculateProjectDailyStats(savedProject, savedFiles);
            savedProject.calculateTotalTimes();

            savedProject = projectStatsRepository.save(savedProject);
            savedProject.setFiles(savedFiles);

            return ResponseWrapper.success(savedProject);
        } catch (ValidationException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseWrapper.error(HttpStatus.BAD_REQUEST, e.getMessage(), "VALIDATION_ERROR");
        } catch (Exception e) {
            logger.error("Error creating project: {}", e.getMessage(), e);
            return ResponseWrapper.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create project", "INTERNAL_ERROR");
        }
    }

    @Override
    @Transactional
    public ResponseWrapper<ProjectStats> updateProjectStats(String projectId, ProjectStats stats, Jwt jwt) {
        try {
            String userId = jwt.getSubject();
            ProjectStats existingProject = projectStatsRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

            if (existingProject.getUser() == null || !existingProject.getUser().getId().equals(userId) && !SecurityUtils.isAdmin(jwt)) {
                throw new UnauthorizedException("You do not have access to this project");
            }

            existingProject.setProjectPath(stats.getProjectPath() != null ? stats.getProjectPath() : existingProject.getProjectPath());
            existingProject.setGithubBadgeVisible(stats.isGithubBadgeVisible());

            if (stats.getDailyStats() != null && !stats.getDailyStats().isEmpty()) {
                existingProject.setDailyStats(stats.getDailyStats());
            }

            existingProject = projectStatsRepository.save(existingProject);
            fileStatsRepository.deleteByProjectId(projectId);

            List<FileStats> savedFiles = new ArrayList<>();
            if (stats.getFiles() != null) {
                for (FileStats file : stats.getFiles()) {
                    validateFileStats(file);
                    file.setProjectId(projectId);

                    if (file.getDailyStats() != null && !file.getDailyStats().isEmpty()) {
                        file.calculateTotalTimes();
                    }

                    FileStats savedFile = fileStatsRepository.save(file);
                    savedFiles.add(savedFile);
                }
            }

            calculateProjectDailyStats(existingProject, savedFiles);
            existingProject.calculateTotalTimes();

            existingProject = projectStatsRepository.save(existingProject);
            existingProject.setFiles(savedFiles);

            return ResponseWrapper.success(existingProject);
        } catch (ResourceNotFoundException e) {
            logger.error("Project not found: {}", e.getMessage());
            return ResponseWrapper.error(HttpStatus.NOT_FOUND, e.getMessage(), "NOT_FOUND");
        } catch (UnauthorizedException e) {
            logger.error("Unauthorized access: {}", e.getMessage());
            return ResponseWrapper.error(HttpStatus.FORBIDDEN, e.getMessage(), "UNAUTHORIZED");
        } catch (ValidationException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseWrapper.error(HttpStatus.BAD_REQUEST, e.getMessage(), "VALIDATION_ERROR");
        } catch (Exception e) {
            logger.error("Error updating project: {}", e.getMessage(), e);
            return ResponseWrapper.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update project", "INTERNAL_ERROR");
        }
    }

    @Override
    @Transactional
    public ResponseWrapper<ProjectStats> patchProjectStats(String projectId, ProjectStats updates, Jwt jwt) {
        try {
            String userId = jwt.getSubject();
            ProjectStats existingProject = projectStatsRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

            if (existingProject.getUser() == null || !existingProject.getUser().getId().equals(userId) && !SecurityUtils.isAdmin(jwt)) {
                throw new UnauthorizedException("You do not have access to this project");
            }

            if (updates.getProjectPath() != null) {
                existingProject.setProjectPath(updates.getProjectPath());
            }
            if (updates.isGithubBadgeVisible()) {
                existingProject.setGithubBadgeVisible(true);
            }else {
                existingProject.setGithubBadgeVisible(false);
            }

            if (updates.getDailyStats() != null && !updates.getDailyStats().isEmpty()) {
                existingProject.getDailyStats().putAll(updates.getDailyStats());
            }

            existingProject = projectStatsRepository.save(existingProject);

            List<FileStats> savedFiles = new ArrayList<>();
            if (updates.getFiles() != null) {
                fileStatsRepository.deleteByProjectId(projectId);
                for (FileStats file : updates.getFiles()) {
                    validateFileStats(file);
                    file.setProjectId(projectId);

                    if (file.getDailyStats() != null && !file.getDailyStats().isEmpty()) {
                        file.calculateTotalTimes();
                    }

                    FileStats savedFile = fileStatsRepository.save(file);
                    savedFiles.add(savedFile);
                }
            } else {
                savedFiles = fileStatsRepository.findByProjectId(projectId);
            }

            calculateProjectDailyStats(existingProject, savedFiles);
            existingProject.calculateTotalTimes();

            existingProject = projectStatsRepository.save(existingProject);
            existingProject.setFiles(savedFiles);

            return ResponseWrapper.success(existingProject);
        } catch (ResourceNotFoundException e) {
            logger.error("Project not found: {}", e.getMessage());
            return ResponseWrapper.error(HttpStatus.NOT_FOUND, e.getMessage(), "NOT_FOUND");
        } catch (UnauthorizedException e) {
            logger.error("Unauthorized access: {}", e.getMessage());
            return ResponseWrapper.error(HttpStatus.FORBIDDEN, e.getMessage(), "UNAUTHORIZED");
        } catch (ValidationException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseWrapper.error(HttpStatus.BAD_REQUEST, e.getMessage(), "VALIDATION_ERROR");
        } catch (Exception e) {
            logger.error("Error patching project: {}", e.getMessage(), e);
            return ResponseWrapper.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to patch project", "INTERNAL_ERROR");
        }
    }

    @Override
    @Transactional
    public ResponseWrapper<String> deleteProject(String projectId, Jwt jwt) {
        try {
            String userId = jwt.getSubject();
            ProjectStats project = projectStatsRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

            if (project.getUser() == null || !project.getUser().getId().equals(userId) && !SecurityUtils.isAdmin(jwt)) {
                throw new UnauthorizedException("You do not have access to this project");
            }

            fileStatsRepository.deleteByProjectId(projectId);
            projectStatsRepository.deleteById(projectId);

            return ResponseWrapper.success("Project deleted successfully");
        } catch (ResourceNotFoundException e) {
            logger.error("Project not found: {}", e.getMessage());
            return ResponseWrapper.error(HttpStatus.NOT_FOUND, e.getMessage(), "NOT_FOUND");
        } catch (UnauthorizedException e) {
            logger.error("Unauthorized access: {}", e.getMessage());
            return ResponseWrapper.error(HttpStatus.FORBIDDEN, e.getMessage(), "UNAUTHORIZED");
        } catch (Exception e) {
            logger.error("Error deleting project: {}", e.getMessage(), e);
            return ResponseWrapper.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete project", "INTERNAL_ERROR");
        }
    }

    @Override
    public ResponseWrapper<List<FileStats>> getProjectFiles(String projectId) {
        try {
            if (!projectStatsRepository.existsById(projectId)) {
                throw new ResourceNotFoundException("Project not found with ID: " + projectId);
            }

            List<FileStats> files = fileStatsRepository.findByProjectId(projectId);
            return ResponseWrapper.success(files);
        } catch (ResourceNotFoundException e) {
            logger.error("Project not found: {}", e.getMessage());
            return ResponseWrapper.error(HttpStatus.NOT_FOUND, e.getMessage(), "NOT_FOUND");
        } catch (Exception e) {
            logger.error("Error retrieving project files: {}", e.getMessage(), e);
            return ResponseWrapper.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve project files", "INTERNAL_ERROR");
        }
    }

    @Override
    public ResponseWrapper<Map<String, Object>> getDashboardStats(Jwt jwt) {
        try {
            String userId = jwt.getSubject();
            List<ProjectStats> projects = SecurityUtils.isAdmin(jwt)
                    ? projectStatsRepository.findAll()
                    : projectStatsRepository.findByUserId(userId);

            long totalCodingTime = projects.stream()
                    .mapToLong(ProjectStats::getTotalCodingTime)
                    .sum();
            long totalOpenTime = projects.stream()
                    .mapToLong(ProjectStats::getTotalOpenTime)
                    .sum();
            long projectCount = projects.size();
            double averageCodingTimePerProject = projectCount > 0 ? (double) totalCodingTime / projectCount : 0;

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProjects", projectCount);
            stats.put("totalCodingTime", totalCodingTime);
            stats.put("totalOpenTime", totalOpenTime);
            stats.put("averageCodingTimePerProject", averageCodingTimePerProject);

            return ResponseWrapper.success(stats);
        } catch (Exception e) {
            logger.error("Error retrieving dashboard stats: {}", e.getMessage(), e);
            return ResponseWrapper.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve dashboard stats", "INTERNAL_ERROR");
        }
    }

//    @Override
//    public ResponseWrapper<List<Map<String, Object>>> getTeamMemberProjects(Jwt jwt) {
//        try {
//            String userId = jwt.getSubject();
//            User user = userRepository.findById(userId)
//                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
//
//            // Assume User has a teamId field; replace with actual team retrieval logic
//            Long teamId = user.getTeamId(); // Placeholder; replace with actual field
//            if (teamId == null) {
//                throw new ResourceNotFoundException("User is not assigned to any team");
//            }
//
//            List<User> teamMembers = userRepository.findByTeamId(teamId);
//            List<Map<String, Object>> teamProjects = new ArrayList<>();
//
//            for (User member : teamMembers) {
//                List<ProjectStats> projects = projectStatsRepository.findByUserId(member.getId());
//                Map<String, Object> memberData = new HashMap<>();
//                memberData.put("userId", member.getId());
//                memberData.put("projects", projects);
//                teamProjects.add(memberData);
//            }
//
//            return ResponseWrapper.success(teamProjects);
//        } catch (ResourceNotFoundException e) {
//            logger.error("Resource not found: {}", e.getMessage());
//            return ResponseWrapper.error(HttpStatus.NOT_FOUND, e.getMessage(), "NOT_FOUND");
//        } catch (Exception e) {
//            logger.error("Error retrieving team member projects: {}", e.getMessage(), e);
//            return ResponseWrapper.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve team member projects", "INTERNAL_ERROR");
//        }
//    }
//
//    @Override
//    public ResponseWrapper<List<ProjectStats>> getProjectsByTeamId(Long teamId, Jwt jwt) {
//        try {
//            // Assume team exists; replace with actual team repository check
//            List<User> teamMembers = userRepository.findByTeamId(teamId);
//            if (teamMembers.isEmpty()) {
//                throw new ResourceNotFoundException("Team not found with ID: " + teamId);
//            }
//
//            // Verify user is part of the team or admin
//            String userId = jwt.getSubject();
//            User user = userRepository.findById(userId)
//                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
//            if (!SecurityUtils.isAdmin(jwt) && (user.getTeamId() == null || !user.getTeamId().equals(teamId))) {
//                throw new UnauthorizedException("You do not have access to this team's projects");
//            }
//
//            List<ProjectStats> projects = new ArrayList<>();
//            for (User member : teamMembers) {
//                projects.addAll(projectStatsRepository.findByUserId(member.getId()));
//            }
//
//            for (ProjectStats project : projects) {
//                List<FileStats> files = fileStatsRepository.findByProjectId(project.getProjectId());
//                project.setFiles(files);
//            }
//
//            return ResponseWrapper.success(projects);
//        } catch (ResourceNotFoundException e) {
//            logger.error("Team not found: {}", e.getMessage());
//            return ResponseWrapper.error(HttpStatus.NOT_FOUND, e.getMessage(), "NOT_FOUND");
//        } catch (UnauthorizedException e) {
//            logger.error("Unauthorized access: {}", e.getMessage());
//            return ResponseWrapper.error(HttpStatus.FORBIDDEN, e.getMessage(), "UNAUTHORIZED");
//        } catch (Exception e) {
//            logger.error("Error retrieving projects by team ID: {}", e.getMessage(), e);
//            return ResponseWrapper.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve team projects", "INTERNAL_ERROR");
//        }
//    }

    @Override
    public String generateBadge(String label, String value, String color) {
        int labelWidth = 60 + label.length() * 6;
        int valueWidth = 60 + value.length() * 6;
        int totalWidth = labelWidth + valueWidth;

        return String.format("""
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="20">
              <linearGradient id="b" x2="0" y2="100%%">
                <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
                <stop offset="1" stop-opacity=".1"/>
              </linearGradient>
              <mask id="a">
                <rect width="%d" height="20" rx="3" fill="#fff"/>
              </mask>
              <g mask="url(#a)">
                <rect width="%d" height="20" fill="#555"/>
                <rect x="%d" width="%d" height="20" fill="%s"/>
                <rect width="%d" height="20" fill="url(#b)"/>
              </g>
              <g fill="#fff" text-anchor="middle" font-family="Verdana,Geneva,DejaVu Sans,sans-serif" font-size="11">
                <text x="%d" y="15" fill="#010101" fill-opacity=".3">%s</text>
                <text x="%d" y="15">%s</text>
                <text x="%d" y="15" fill="#010101" fill-opacity=".3">%s</text>
                <text x="%d" y="15">%s</text>
              </g>
            </svg>
            """,
                totalWidth,
                totalWidth,
                labelWidth,
                labelWidth,
                valueWidth,
                color,
                totalWidth,
                labelWidth / 2, label,
                labelWidth / 2, label,
                labelWidth + valueWidth / 2, value,
                labelWidth + valueWidth / 2, value
        );
    }

    private void calculateProjectDailyStats(ProjectStats project, List<FileStats> files) {
        Map<LocalDate, DailyStats> projectDailyStats = new HashMap<>();

        for (FileStats file : files) {
            if (file.getDailyStats() != null) {
                for (Map.Entry<LocalDate, DailyStats> entry : file.getDailyStats().entrySet()) {
                    LocalDate date = entry.getKey();
                    DailyStats fileDailyStats = entry.getValue();

                    DailyStats projectStats = projectDailyStats.computeIfAbsent(date, k -> new DailyStats());
                    long currentCodingTime = projectStats.getCodingTime() != null ? projectStats.getCodingTime() : 0L;
                    long currentOpenTime = projectStats.getOpenTime() != null ? projectStats.getOpenTime() : 0L;

                    projectStats.setCodingTime(currentCodingTime + fileDailyStats.getCodingTime());
                    projectStats.setOpenTime(currentOpenTime + fileDailyStats.getOpenTime());
                }
            }
        }

        project.setDailyStats(projectDailyStats);
    }

    private void validateFileStats(FileStats file) {
        if (file.getFilePath() == null || file.getFilePath().trim().isEmpty()) {
            throw new ValidationException("File path is required");
        }
        if (file.getType() == null || file.getType().trim().isEmpty()) {
            throw new ValidationException("File type is required");
        }

        if (file.getDailyStats() == null || file.getDailyStats().isEmpty()) {
            if (file.getCodingTime() == null || file.getOpenTime() == null) {
                throw new ValidationException("Either daily stats or total times must be provided");
            }
        } else {
            for (Map.Entry<LocalDate, DailyStats> entry : file.getDailyStats().entrySet()) {
                DailyStats dailyStats = entry.getValue();
                if (dailyStats.getCodingTime() == null || dailyStats.getOpenTime() == null) {
                    throw new ValidationException("Daily stats must have both coding time and open time");
                }
            }
        }
    }

    @Override
    public ProjectStats findProjectById(String projectId) {
        ProjectStats project = projectStatsRepository.findById(projectId).orElse(null);
        if (project != null) {
            List<FileStats> files = fileStatsRepository.findByProjectId(projectId);
            project.setFiles(files);
        }
        return project;
    }
}