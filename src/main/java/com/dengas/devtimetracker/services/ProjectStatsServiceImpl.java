package com.dengas.devtimetracker.services;

import com.dengas.devtimetracker.exceptions.ResourceNotFoundException;
import com.dengas.devtimetracker.exceptions.UnauthorizedException;
import com.dengas.devtimetracker.factory.UserFactory;
import com.dengas.devtimetracker.model.FileStats;
import com.dengas.devtimetracker.model.ProjectStats;
import com.dengas.devtimetracker.model.User;
import com.dengas.devtimetracker.repositories.FileStatsRepository;
import com.dengas.devtimetracker.repositories.ProjectStatsRepository;
import com.dengas.devtimetracker.repositories.UserRepository;
import com.dengas.devtimetracker.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProjectStatsServiceImpl implements ProjectStatsService {

    private final ProjectStatsRepository projectStatsRepository;
    private final FileStatsRepository fileStatsRepository;
    private final UserRepository userRepository;
    private final UserFactory userFactory;

    @Override
    public List<ProjectStats> getAllProjectStats(Jwt jwt) {
        if (SecurityUtils.isAdmin(jwt)) {
            return projectStatsRepository.findAll(); 
        } else {
            String userId = jwt.getSubject();
            return projectStatsRepository.findByUserId(userId);
        }
    }

    @Override
    public ProjectStats getProjectStats(String projectId, Jwt jwt) {
        String userId = jwt.getSubject();
        ProjectStats project = projectStatsRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Проект не знайдено з ID: " + projectId));
        
        if (project.getUser() != null && !project.getUser().getId().equals(userId) && !SecurityUtils.isAdmin(jwt)) {
            throw new UnauthorizedException("У вас немає доступу до цього проекту");
        }
        
        return project;
    }
    
    @Override
    public ProjectStats createProject(ProjectStats stats, Jwt jwt) {
        String projectId = UUID.randomUUID().toString();
        stats.setProjectId(projectId);
        stats.setGithubBadgeVisible(false);
        
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
        return projectStatsRepository.save(stats);
    }

    @Override
    public ProjectStats updateProjectStats(String projectId, ProjectStats stats, Jwt jwt) {
        // Отримуємо ID користувача з JWT токена
        String userId = jwt.getSubject();
        
        // Перевіряємо, чи існує проект з таким ID
        ProjectStats existingProject = projectStatsRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Проект не знайдено з ID: " + projectId));
        
        // Перевіряємо, чи має користувач доступ до проекту
        if (existingProject.getUser() != null && !existingProject.getUser().getId().equals(userId)) {
            // Перевіряємо, чи є користувач адміністратором
            boolean isAdmin = jwt.getClaimAsMap("resource_access")
                    .containsKey("devTimeTracker-rest-api") &&
                    ((Map<String, List<String>>) jwt.getClaimAsMap("resource_access")
                            .get("devTimeTracker-rest-api"))
                            .get("roles").contains("ADMIN");
            
            if (!isAdmin) {
                throw new UnauthorizedException("У вас немає доступу до цього проекту");
            }
        }
        
        // Оновлюємо поля проекту
        existingProject.setProjectPath(stats.getProjectPath());
        existingProject.setTotalCodingTime(stats.getTotalCodingTime());
        existingProject.setTotalOpenTime(stats.getTotalOpenTime());
        existingProject.setGithubBadgeVisible(stats.isGithubBadgeVisible());
        existingProject.setGithubBadgeLink(stats.getGithubBadgeLink());
        existingProject.setDailyStats(stats.getDailyStats());
        
        return projectStatsRepository.save(existingProject);
    }

    @Override
    public List<FileStats> getProjectFiles(String projectId) {
        // Перевіряємо, чи існує проект
        if (!projectStatsRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Проект не знайдено з ID: " + projectId);
        }
        
        return fileStatsRepository.findByProjectId(projectId);
    }

    @Override
    public Object getDashboardStats() {
        // Тимчасова заглушка - тут має бути код для отримання статистики для дашборду
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Dashboard stats not implemented yet");
        return result;
    }

    @Override
    public Object getTeamMemberProjects() {
        // Тимчасова заглушка - тут має бути код для отримання проектів членів команди
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Team member projects not implemented yet");
        return result;
    }

    @Override
    public Object getProjectsByTeamId(Long teamId) {
        // Тимчасова заглушка - тут має бути код для отримання проектів за ID команди
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Projects by team ID not implemented yet");
        return result;
    }
}