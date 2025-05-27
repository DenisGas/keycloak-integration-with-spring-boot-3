package com.dengas.devtimetracker.repositories;

import com.dengas.devtimetracker.model.ProjectStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectStatsRepository extends JpaRepository<ProjectStats, String> {
    List<ProjectStats> findByUserId(String userId);
}