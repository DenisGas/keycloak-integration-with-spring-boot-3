package com.dengas.devtimetracker.repositories;

import com.dengas.devtimetracker.model.FileStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileStatsRepository extends JpaRepository<FileStats, Long> {
    List<FileStats> findByProjectId(String projectId);
}