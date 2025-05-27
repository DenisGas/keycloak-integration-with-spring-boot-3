package com.dengas.devtimetracker.services;

import com.dengas.devtimetracker.model.Team;

public interface TeamService {
    Team createTeam(Team team);
    Team addMember(Long teamId, String userId);
    void removeMember(Long teamId, String userId);
    Team assignTeamLead(Long teamId, String userId);
    Object getTeamProjects(Long teamId);
}