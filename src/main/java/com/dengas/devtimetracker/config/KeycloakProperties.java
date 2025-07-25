package com.dengas.devtimetracker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String tokenUri;
    private String realm;
    private String authUri;    // http://localhost:8080/realms/devTimeTracker/protocol/openid-connect/auth
    private String logoutUri;  // http://localhost:8080/realms/devTimeTracker/protocol/openid-connect/logout
}
