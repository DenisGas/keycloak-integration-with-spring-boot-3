package com.dengas.devtimetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос для аутентификации по логину и паролю")
public class LoginRequest {

    @Schema(description = "User name", example = "den", required = true)
    private String username;

    @Schema(description = "User password", example = "2004", required = true)
    private String password;

    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
