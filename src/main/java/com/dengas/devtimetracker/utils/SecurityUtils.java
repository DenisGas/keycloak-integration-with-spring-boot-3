package com.dengas.devtimetracker.utils;

import org.springframework.security.oauth2.jwt.Jwt;
import java.util.List;
import java.util.Map;

public class SecurityUtils {
    public static boolean isAdmin(Jwt jwt) {
        try {
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null && resourceAccess.containsKey("devTimeTracker-rest-api")) {
                Map<String, Object> resource = (Map<String, Object>) resourceAccess.get("devTimeTracker-rest-api");
                if (resource != null && resource.get("roles") != null) {
                    List<String> roles = (List<String>) resource.get("roles");
                    return roles.contains("client_admin");
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}