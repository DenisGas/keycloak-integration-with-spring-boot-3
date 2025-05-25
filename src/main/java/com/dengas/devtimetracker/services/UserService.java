package com.dengas.devtimetracker.services;

import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class UserService {

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, String> getUserInfo(Jwt jwt) {
        return Map.of(
                "email", jwt.getClaimAsString("email"),
                "name", jwt.getClaimAsString("preferred_username")
        );
    }
}

