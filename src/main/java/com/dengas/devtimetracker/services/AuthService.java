package com.dengas.devtimetracker.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AuthService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper(); // 👈 Додали Jackson

    public ResponseEntity<?> exchangeCode(String code, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("client_id", "devTimeTracker-rest-api");
        form.add("client_secret", "t0JJg0MQxvyiZoErK6Gy8hmdpcQgjYFC");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        String tokenEndpoint = "http://localhost:8080/realms/devTimeTracker/protocol/openid-connect/token";

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenEndpoint, request, String.class);
            Map<String, Object> tokens = objectMapper.readValue(response.getBody(), Map.class); // 👈 Парсимо JSON у Map

            // Повертаємо тільки потрібні токени
            return ResponseEntity.ok(Map.of(
                    "access_token", tokens.get("access_token"),
                    "id_token", tokens.get("id_token"),
                    "expires_in", tokens.get("expires_in")
            ));

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Error exchanging code"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal error"));
        }
    }
}
