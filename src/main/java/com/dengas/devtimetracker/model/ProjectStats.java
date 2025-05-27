package com.dengas.devtimetracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProjectStats {
    @Id
    private String projectId;
    
    private String projectPath;
    private long totalCodingTime;
    private long totalOpenTime;
    
    private boolean githubBadgeVisible = false; // За замовчуванням false
    private String githubBadgeLink;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ElementCollection
    @CollectionTable(name = "project_daily_stats")
    private Map<String, DailyStats> dailyStats = new HashMap<>();
}