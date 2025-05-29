package com.dengas.devtimetracker.services.implement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
        List<ProjectStats> projects;
        if (SecurityUtils.isAdmin(jwt)) {
            projects = projectStatsRepository.findAll();
        } else {
            String userId = jwt.getSubject();
            projects = projectStatsRepository.findByUserId(userId);
        }

        // Загружаем файлы для каждого проекта
        for (ProjectStats project : projects) {
            List<FileStats> files = fileStatsRepository.findByProjectId(project.getProjectId());
            project.setFiles(files);
        }

        return projects;
    }

    @Override
    public ProjectStats getProjectStats(String projectId, Jwt jwt) {
        String userId = jwt.getSubject();
        ProjectStats project = projectStatsRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Проект не знайдено з ID: " + projectId));

        if (project.getUser() != null && !project.getUser().getId().equals(userId) && !SecurityUtils.isAdmin(jwt)) {
            throw new UnauthorizedException("У вас немає доступу до цього проекту");
        }

        // Загружаем файлы проекта
        List<FileStats> files = fileStatsRepository.findByProjectId(projectId);
        project.setFiles(files);

        return project;
    }

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

        // Спочатку зберігаємо сам проект (без файлів)
        ProjectStats savedProject = projectStatsRepository.save(stats);

        // Потім обробляємо файли
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

        // Розраховуємо загальну статистику проекту
        calculateProjectDailyStats(savedProject, savedFiles);
        savedProject.calculateTotalTimes();

        // Зберігаємо оновлений проект зі статистикою
        savedProject = projectStatsRepository.save(savedProject);

        // Повертаємо проект з файлами
        savedProject.setFiles(savedFiles);
        return savedProject;
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

        // Проверяем наличие dailyStats
        if (file.getDailyStats() == null || file.getDailyStats().isEmpty()) {
            // Если нет dailyStats, должны быть установлены общие времена
            if (file.getCodingTime() == null || file.getOpenTime() == null) {
                throw new ValidationException("Either daily stats or total times must be provided");
            }
        } else {
            // Проверяем валидность dailyStats
            for (Map.Entry<LocalDate, DailyStats> entry : file.getDailyStats().entrySet()) {
                DailyStats dailyStats = entry.getValue();
                if (dailyStats.getCodingTime() == null || dailyStats.getOpenTime() == null) {
                    throw new ValidationException("Daily stats must have both coding time and open time");
                }
            }
        }
    }

    @Override
    @Transactional
    public ProjectStats updateProjectStats(String projectId, ProjectStats stats, Jwt jwt) {
        Logger logger = LoggerFactory.getLogger(ProjectStatsServiceImpl.class);
        logger.debug("Початок оновлення проєкту з ID: {}", projectId);

        try {
            String userId = jwt.getSubject();
            logger.debug("Отримано userId з JWT: {}", userId);

            // Перевіряємо існування проєкту
            logger.debug("Шукаємо проєкт з ID: {}", projectId);
            ProjectStats existingProject = projectStatsRepository.findById(projectId)
                    .orElseThrow(() -> {
                        logger.error("Проєкт не знайдено з ID: {}", projectId);
                        return new ResourceNotFoundException("Проєкт не знайдено з ID: " + projectId);
                    });

            // Перевіряємо права доступу
            logger.debug("Перевірка прав доступу для користувача: {}", userId);
            if (existingProject.getUser() == null || !existingProject.getUser().getId().equals(userId) && !SecurityUtils.isAdmin(jwt)) {
                logger.error("Користувач {} не має доступу до проєкту {} або користувач не встановлений", userId, projectId);
                throw new UnauthorizedException("У вас немає доступу до цього проєкту");
            }

            // Оновлюємо основні поля проєкту
            logger.debug("Оновлення основних полів проєкту");
            existingProject.setProjectPath(stats.getProjectPath() != null ? stats.getProjectPath() : existingProject.getProjectPath());
            existingProject.setGithubBadgeVisible(stats.isGithubBadgeVisible());

            // Оновлюємо dailyStats, якщо вони передані
            if (stats.getDailyStats() != null && !stats.getDailyStats().isEmpty()) {
                logger.debug("Оновлення dailyStats для проєкту");
                existingProject.setDailyStats(stats.getDailyStats());
            }

            // Зберігаємо проєкт перед обробкою файлів
            logger.debug("Збереження проєкту перед обробкою файлів");
            existingProject = projectStatsRepository.save(existingProject);
            logger.debug("Проєкт збережено з ID: {}", existingProject.getProjectId());

            // Видаляємо всі існуючі файли проєкту
            logger.debug("Видалення всіх файлів для проєкту з ID: {}", projectId);
            fileStatsRepository.deleteByProjectId(projectId);

            // Обробляємо нові файли
            List<FileStats> savedFiles = new ArrayList<>();
            if (stats.getFiles() != null) {
                logger.debug("Обробка {} файлів для проєкту", stats.getFiles().size());
                for (FileStats file : stats.getFiles()) {
                    logger.debug("Валідація файлу: {}", file.getFilePath());
                    validateFileStats(file);
                    file.setProjectId(projectId);

                    // Перераховуємо totalCodingTime і totalOpenTime для файлу
                    if (file.getDailyStats() != null && !file.getDailyStats().isEmpty()) {
                        logger.debug("Перерахунок часу для файлу: {}", file.getFilePath());
                        file.calculateTotalTimes();
                    }

                    logger.debug("Збереження файлу: {}", file.getFilePath());
                    FileStats savedFile = fileStatsRepository.save(file);
                    savedFiles.add(savedFile);
                }
            } else {
                logger.debug("Файли не передані для оновлення");
            }

            // Перераховуємо dailyStats проєкту на основі файлів
            logger.debug("Перерахунок dailyStats для проєкту");
            calculateProjectDailyStats(existingProject, savedFiles);
            existingProject.calculateTotalTimes();

            // Зберігаємо оновлений проєкт
            logger.debug("Збереження оновленого проєкту");
            existingProject = projectStatsRepository.save(existingProject);

            // Завантажуємо файли для повернення в відповіді
            logger.debug("Встановлення файлів у відповідь: {} файлів", savedFiles.size());
            existingProject.setFiles(savedFiles);

            logger.info("Проєкт з ID: {} успішно оновлено", projectId);
            return existingProject;

        } catch (Exception e) {
            logger.error("Помилка при оновленні проєкту з ID: {}. Деталі: {}", projectId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<FileStats> getProjectFiles(String projectId) {
        if (!projectStatsRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Проект не знайдено з ID: " + projectId);
        }

        return fileStatsRepository.findByProjectId(projectId);
    }

    @Override
    public Object getDashboardStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Dashboard stats not implemented yet");
        return result;
    }

    @Override
    public Object getTeamMemberProjects() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Team member projects not implemented yet");
        return result;
    }

    @Override
    public Object getProjectsByTeamId(Long teamId) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Projects by team ID not implemented yet");
        return result;
    }


    @Override
    @Transactional
    public ProjectStats patchProjectStats(String projectId, ProjectStats updates, Jwt jwt) {
        Logger logger = LoggerFactory.getLogger(ProjectStatsServiceImpl.class);
        logger.debug("Початок часткового оновлення проєкту з ID: {}", projectId);

        try {
            String userId = jwt.getSubject();
            logger.debug("Отримано userId з JWT: {}", userId);

            // Перевіряємо існування проєкту
            logger.debug("Шукаємо проєкт з ID: {}", projectId);
            ProjectStats existingProject = projectStatsRepository.findById(projectId)
                    .orElseThrow(() -> {
                        logger.error("Проєкт не знайдено з ID: {}", projectId);
                        return new ResourceNotFoundException("Проєкт не знайдено з ID: " + projectId);
                    });

            // Перевіряємо права доступу
            logger.debug("Перевірка прав доступу для користувача: {}", userId);
            if (existingProject.getUser() == null || !existingProject.getUser().getId().equals(userId) && !SecurityUtils.isAdmin(jwt)) {
                logger.error("Користувач {} не має доступу до проєкту {} або користувач не встановлений", userId, projectId);
                throw new UnauthorizedException("У вас немає доступу до цього проєкту");
            }

            // Оновлюємо лише передані поля
            if (updates.getProjectPath() != null) {
                logger.debug("Оновлення projectPath: {}", updates.getProjectPath());
                existingProject.setProjectPath(updates.getProjectPath());
            }
            if (updates.isGithubBadgeVisible()) {
                logger.debug("Оновлення githubBadgeVisible: {}", updates.isGithubBadgeVisible());
                existingProject.setGithubBadgeVisible(updates.isGithubBadgeVisible());
            }

            // Оновлюємо dailyStats, якщо вони передані
            if (updates.getDailyStats() != null && !updates.getDailyStats().isEmpty()) {
                logger.debug("Оновлення dailyStats для проєкту");
                existingProject.getDailyStats().putAll(updates.getDailyStats());
            }

            // Зберігаємо проєкт перед обробкою файлів
            logger.debug("Збереження проєкту перед обробкою файлів");
            existingProject = projectStatsRepository.save(existingProject);
            logger.debug("Проєкт збережено з ID: {}", existingProject.getProjectId());

            // Обробляємо файли, якщо вони передані
            List<FileStats> savedFiles = new ArrayList<>();
            if (updates.getFiles() != null) {
                logger.debug("Обробка {} файлів для проєкту", updates.getFiles().size());
                // Видаляємо старі файли
                logger.debug("Видалення всіх файлів для проєкту з ID: {}", projectId);
                fileStatsRepository.deleteByProjectId(projectId);

                // Додаємо нові файли
                for (FileStats file : updates.getFiles()) {
                    logger.debug("Валідація файлу: {}", file.getFilePath());
                    validateFileStats(file);
                    file.setProjectId(projectId);

                    // Перераховуємо totalCodingTime і totalOpenTime для файлу
                    if (file.getDailyStats() != null && !file.getDailyStats().isEmpty()) {
                        logger.debug("Перерахунок часу для файлу: {}", file.getFilePath());
                        file.calculateTotalTimes();
                    }

                    logger.debug("Збереження файлу: {}", file.getFilePath());
                    FileStats savedFile = fileStatsRepository.save(file);
                    savedFiles.add(savedFile);
                }
            } else {
                // Якщо файли не передані, завантажуємо існуючі для перерахунку
                logger.debug("Файли не передані, завантаження існуючих файлів");
                savedFiles = fileStatsRepository.findByProjectId(projectId);
            }

            // Перераховуємо dailyStats проєкту на основі файлів
            logger.debug("Перерахунок dailyStats для проєкту");
            calculateProjectDailyStats(existingProject, savedFiles);
            existingProject.calculateTotalTimes();

            // Зберігаємо оновлений проєкт
            logger.debug("Збереження оновленого проєкту");
            existingProject = projectStatsRepository.save(existingProject);

            // Завантажуємо файли для повернення в відповіді
            logger.debug("Встановлення файлів у відповідь: {} файлів", savedFiles.size());
            existingProject.setFiles(savedFiles);

            logger.info("Проєкт з ID: {} успішно частково оновлено", projectId);
            return existingProject;

        } catch (Exception e) {
            logger.error("Помилка при частковому оновленні проєкту з ID: {}. Деталі: {}", projectId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void deleteProject(String projectId, Jwt jwt) {
        String userId = jwt.getSubject();
        ProjectStats project = projectStatsRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Проект не знайдено з ID: " + projectId));

        if (!project.getUser().getId().equals(userId) && !SecurityUtils.isAdmin(jwt)) {
            throw new UnauthorizedException("У вас немає доступу до цього проекту");
        }

        // Удаляем все связанные файлы
        fileStatsRepository.deleteByProjectId(projectId);
        // Удаляем проект
        projectStatsRepository.deleteById(projectId);
    }

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

    public ProjectStats findProjectById(String projectId) {
        ProjectStats project = projectStatsRepository.findById(projectId).orElse(null);
        if (project != null) {
            // Загружаем файлы
            List<FileStats> files = fileStatsRepository.findByProjectId(projectId);
            project.setFiles(files);
        }
        return project;
    }
}