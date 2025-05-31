package com.dengas.devtimetracker.services;

import com.dengas.devtimetracker.config.KeycloakProperties;
import com.dengas.devtimetracker.dto.ResponseWrapper;
import com.dengas.devtimetracker.dto.TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import java.util.Map;

@Service
public class AuthService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KeycloakProperties keycloakProperties;

    public AuthService(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
    }

    public String buildLoginUrl(String redirectUri, boolean forcePrompt) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(keycloakProperties.getAuthUri())
                .queryParam("client_id", keycloakProperties.getClientId())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid")
                .queryParam("redirect_uri", redirectUri);

        if (forcePrompt) {
            builder.queryParam("prompt", "login");
        }

        return builder.build().toUriString();
    }


    public String buildLogoutUrl(String redirectUri, String idToken) {
        return UriComponentsBuilder
                .fromUriString(keycloakProperties.getLogoutUri())
                .queryParam("post_logout_redirect_uri", redirectUri)
                .queryParam("id_token_hint", idToken)
                .build().toUriString();
    }

    public ResponseEntity<?> exchangeCode(String code, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("client_id", keycloakProperties.getClientId());
        form.add("client_secret", keycloakProperties.getClientSecret());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        String tokenEndpoint = keycloakProperties.getTokenUri();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenEndpoint, request, String.class);
            Map<String, Object> tokens = objectMapper.readValue(response.getBody(), Map.class);

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

    public ResponseWrapper<TokenResponse> exchangeCredentials(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("username", username);
        form.add("password", password);
        form.add("client_id", keycloakProperties.getClientId());
        form.add("client_secret", keycloakProperties.getClientSecret());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        String tokenEndpoint = keycloakProperties.getTokenUri();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenEndpoint, request, String.class);
            Map<String, Object> tokens = objectMapper.readValue(response.getBody(), Map.class);

            TokenResponse tokenResponse = new TokenResponse(
                    (String) tokens.get("access_token"),
                    null, // id_token не повертається для password grant
                    (String) tokens.get("refresh_token"),
                    (Integer) tokens.get("expires_in")
            );

            return ResponseWrapper.success(tokenResponse);
        } catch (HttpClientErrorException e) {
            return ResponseWrapper.error(HttpStatus.UNAUTHORIZED, "Неверные имя пользователя или пароль", "INVALID_CREDENTIALS");
        } catch (Exception e) {
            return ResponseWrapper.error(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", "INTERNAL_ERROR");
        }
    }

}

