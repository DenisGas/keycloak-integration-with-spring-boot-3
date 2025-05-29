package com.dengas.devtimetracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@Entity
public class FileStats {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "Project ID is required")
    @Column(name = "project_id")
    private String projectId;

    @NotBlank(message = "File path is required")
    private String filePath;

    @NotBlank(message = "File type is required")
    private String type;

    private Long openTime = 0L;
    private Long codingTime = 0L;

    @ElementCollection
    @CollectionTable(name = "file_daily_stats", joinColumns = @JoinColumn(name = "file_id"))
    @MapKeyColumn(name = "date")
    private Map<LocalDate, DailyStats> dailyStats = new HashMap<>();

    // Убираем связь с ProjectStats чтобы избежать проблем с маппингом
    // Вместо этого используем только projectId

    // Метод для обчислення totalCodingTime і totalOpenTime з dailyStats
    public void calculateTotalTimes() {
        if (dailyStats != null && !dailyStats.isEmpty()) {
            this.codingTime = dailyStats.values().stream()
                    .mapToLong(stats -> stats.getCodingTime() != null ? stats.getCodingTime() : 0L)
                    .sum();
            this.openTime = dailyStats.values().stream()
                    .mapToLong(stats -> stats.getOpenTime() != null ? stats.getOpenTime() : 0L)
                    .sum();
        }
    }
}