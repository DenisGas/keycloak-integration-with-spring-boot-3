package com.dengas.devtimetracker.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String keycloakIssuerUri;

    @Value("${keycloak.auth-uri}")
    private String keycloakAuthUri;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());

        // Проверяем подключение к Keycloak (внутреннее)
        try {
            String wellKnownUrl = keycloakIssuerUri + "/.well-known/openid-configuration";
            restTemplate.getForObject(wellKnownUrl, String.class);
            health.put("keycloak_internal", "UP");
        } catch (Exception e) {
            health.put("keycloak_internal", "DOWN: " + e.getMessage());
        }

        // Информация о конфигурации
        health.put("keycloak_issuer_uri", keycloakIssuerUri);
        health.put("keycloak_auth_uri", keycloakAuthUri);

        return ResponseEntity.ok(health);
    }

    @GetMapping("/keycloak")
    public ResponseEntity<Map<String, Object>> keycloakHealth() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Проверяем .well-known конфигурацию Keycloak
            String wellKnownUrl = keycloakIssuerUri + "/.well-known/openid-configuration";
            String response = restTemplate.getForObject(wellKnownUrl, String.class);

            result.put("status", "UP");
            result.put("well_known_url", wellKnownUrl);
            result.put("response_length", response != null ? response.length() : 0);

        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
            result.put("well_known_url", keycloakIssuerUri + "/.well-known/openid-configuration");
        }

        return ResponseEntity.ok(result);
    }
}