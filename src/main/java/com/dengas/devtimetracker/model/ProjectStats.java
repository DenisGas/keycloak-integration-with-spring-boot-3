package com.dengas.devtimetracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Schema(
        name = "ProjectStats",
        description = "Статистика проекту з інформацією про час кодування, файли та щоденну активність"
)
public class ProjectStats {

    @Id
    @Schema(
            description = "Унікальний ідентифікатор проекту",
            example = "24fa6720-c2be-4c93-86fa-e0e0e3f36916",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String projectId;

    @NotBlank(message = "Шлях до проєкту обов'язковий")
    @Schema(
            description = "Шлях до проекту в файловій системі",
            example = "/users/username/projects/my-project",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String projectPath;

    @Schema(
            description = "Загальний час кодування в секундах (автоматично розраховується з dailyStats)",
            example = "3600",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long totalCodingTime = 0L;

    @Schema(
            description = "Загальний час відкриття файлів в секундах (автоматично розраховується з dailyStats)",
            example = "7200",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long totalOpenTime = 0L;

    @Schema(
            description = "Чи відображається GitHub-бейдж для проєкту",
            example = "true"
    )
    private boolean githubBadgeVisible = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Schema(
            description = "Користувач, якому належить проєкт",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private User user;

    @ElementCollection
    @CollectionTable(name = "project_daily_stats", joinColumns = @JoinColumn(name = "project_id"))
    @MapKeyColumn(name = "date")
    @Schema(
            description = "Щоденна статистика проєкту (дата -> статистика)",
            example = """
                    {
                        "2024-01-20": {
                            "codingTime": 400,
                            "openTime": 800
                        },
                        "2024-01-21": {
                            "codingTime": 500,
                            "openTime": 1000
                        }
                    }
                    """
    )
    private Map<LocalDate, DailyStats> dailyStats = new HashMap<>();

    @Transient
    @Schema(
            description = "Список файлів проєкту з їхньою статистикою",
            type = "array",
            example = """
                    [
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
                    """
    )
    private List<FileStats> files;

    @Schema(hidden = true)
    public void calculateTotalTimes() {
        if (dailyStats != null && !dailyStats.isEmpty()) {
            this.totalCodingTime = dailyStats.values().stream()
                    .mapToLong(stats -> stats.getCodingTime() != null ? stats.getCodingTime() : 0L)
                    .sum();
            this.totalOpenTime = dailyStats.values().stream()
                    .mapToLong(stats -> stats.getOpenTime() != null ? stats.getOpenTime() : 0L)
                    .sum();
        }
    }
}