package com.dengas.devtimetracker.dto;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с токенами")
public class TokenResponse {

    @Schema(description = "JWT токен доступа", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "ID токен", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String idToken;

    @Schema(description = "Токен обновления", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "Время жизни токена в секундах", example = "3600")
    private Integer expiresIn;

    public TokenResponse() {}

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }
}
