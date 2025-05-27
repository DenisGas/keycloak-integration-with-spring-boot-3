package com.dengas.devtimetracker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Entity
public class FileStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String projectId;
    private String filePath;
    private String type;
    private long openTime;
    private long codingTime;
    
    @ElementCollection
    @CollectionTable(name = "file_daily_stats")
    private Map<String, DailyStats> dailyStats = new HashMap<>();
}