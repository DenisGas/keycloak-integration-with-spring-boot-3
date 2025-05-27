package com.dengas.devtimetracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    @Id
    private String id;  // Keycloak user ID
    
    private String email;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @ManyToMany(mappedBy = "members")
    private Set<Team> teams = new HashSet<>();
    
    @OneToOne(mappedBy = "teamLead")
    private Team leadingTeam;
    
    public String getName() {
        return username;
    }
    
    public boolean isTeamLead() {
        return leadingTeam != null;
    }
    
    public Long getLeadingTeamId() {
        return leadingTeam != null ? leadingTeam.getId() : null;
    }
    
    public Set<Long> getTeamIds() {
        Set<Long> teamIds = new HashSet<>();
        for (Team team : teams) {
            teamIds.add(team.getId());
        }
        return teamIds;
    }
}