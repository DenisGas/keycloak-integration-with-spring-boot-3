package com.dengas.devtimetracker.services;

import com.dengas.devtimetracker.exceptions.ResourceNotFoundException;
import com.dengas.devtimetracker.factory.UserFactory;
import com.dengas.devtimetracker.model.User;
import com.dengas.devtimetracker.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserFactory userFactory;

    public Map<String, Object> getUserInfo(Jwt jwt) {
        String userId = jwt.getSubject();
        User user = userRepository.findById(userId)
                .map(this::checkAndUpdateUser)
                .orElseGet(() -> createUser(jwt));

        return getUserInfoMap(user);
    }

    public Map<String, Object> getUserInfoById(String userId) {
        User user = userRepository.findById(userId)
                .map(this::checkAndUpdateUser)
                .orElseThrow(() -> new ResourceNotFoundException("Користувача не знайдено"));

        return getUserInfoMap(user);
    }

    private User checkAndUpdateUser(User user) {
        User updatedUser = userFactory.updateIfNeeded(user);
        return updatedUser != null ? userRepository.save(updatedUser) : user;
    }

    private User createUser(Jwt jwt) {
        User user = userFactory.createFromJwt(jwt);
        return userRepository.save(user);
    }

    private Map<String, Object> getUserInfoMap(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("name", user.getName());
        userInfo.put("createdAt", user.getCreatedAt());
        userInfo.put("updatedAt", user.getUpdatedAt());
        userInfo.put("isTeamLead", user.isTeamLead());
        userInfo.put("teamIds", user.getTeamIds());
        
        if (user.isTeamLead()) {
            userInfo.put("leadingTeamId", user.getLeadingTeamId());
        }
        
        return userInfo;
    }
}

