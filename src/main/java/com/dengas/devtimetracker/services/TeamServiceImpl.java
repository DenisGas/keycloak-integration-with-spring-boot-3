package com.dengas.devtimetracker.services;

import com.dengas.devtimetracker.model.Team;
import com.dengas.devtimetracker.model.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TeamServiceImpl implements TeamService {

    @Override
    public Team createTeam(Team team) {
        // Тимчасова заглушка - тут має бути код для створення команди
        return team;
    }

    @Override
    public Team addMember(Long teamId, String userId) {
        // Тимчасова заглушка - тут має бути код для додавання учасника
        Team team = new Team();
        team.setId(teamId);
        return team;
    }

    @Override
    public void removeMember(Long teamId, String userId) {
        // Тимчасова заглушка - тут має бути код для видалення учасника
    }

    @Override
    public Team assignTeamLead(Long teamId, String userId) {
        // Тимчасова заглушка - тут має бути код для призначення керівника команди
        Team team = new Team();
        team.setId(teamId);
        User lead = new User();
        lead.setId(userId);
        team.setTeamLead(lead);
        return team;
    }

    @Override
    public Object getTeamProjects(Long teamId) {
        // Тимчасова заглушка - тут має бути код для отримання проектів команди
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Team projects not implemented yet");
        return result;
    }
}