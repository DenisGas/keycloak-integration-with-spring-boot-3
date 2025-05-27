package com.dengas.devtimetracker.factory;

import com.dengas.devtimetracker.model.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserFactory {

    public User createFromJwt(Jwt jwt) {
        User user = new User();
        user.setId(jwt.getSubject());
        user.setEmail(jwt.getClaimAsString("email"));
        user.setUsername(jwt.getClaimAsString("name"));
        initializeDates(user);
        return user;
    }

    public User updateIfNeeded(User user) {
        boolean needsUpdate = false;

        if (user.getCreatedAt() == null || user.getUpdatedAt() == null) {
            initializeDates(user);
            needsUpdate = true;
        }

        if (user.getUsername() == null && user.getEmail() != null) {
            user.setUsername(user.getEmail().split("@")[0]);
            needsUpdate = true;
        }

        return needsUpdate ? user : null;
    }

    private void initializeDates(User user) {
        LocalDateTime now = LocalDateTime.now();
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(now);
        }
        user.setUpdatedAt(now);
    }
}