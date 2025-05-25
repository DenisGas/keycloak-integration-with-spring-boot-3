package com.dengas.devtimetracker.controllers;
import com.dengas.devtimetracker.services.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public void login(@RequestParam String redirect_uri, HttpServletResponse response) throws IOException {
        String loginUrl = UriComponentsBuilder
                .fromUriString("http://localhost:8080/realms/devTimeTracker/protocol/openid-connect/auth")
                .queryParam("client_id", "devTimeTracker-rest-api")
                .queryParam("response_type", "code")
                .queryParam("scope", "openid")
                .queryParam("redirect_uri", redirect_uri)
                .queryParam("prompt", "login")
                .build().toUriString();

        response.sendRedirect(loginUrl);
    }

    @GetMapping("/logout")
    public void logout(@RequestParam String redirect_uri,
                       @RequestParam String id_token_hint,
                       HttpServletResponse response) throws IOException {
        String logoutUrl = UriComponentsBuilder
                .fromUriString("http://localhost:8080/realms/devTimeTracker/protocol/openid-connect/logout")
                .queryParam("post_logout_redirect_uri", redirect_uri)
                .queryParam("id_token_hint", id_token_hint)
                .build().toUriString();

        response.sendRedirect(logoutUrl);
    }



    @PostMapping("/code")
    public ResponseEntity<?> exchangeCode(@RequestBody Map<String, String> body) {
        return authService.exchangeCode(body.get("code"), body.get("redirect_uri"));
    }
}

