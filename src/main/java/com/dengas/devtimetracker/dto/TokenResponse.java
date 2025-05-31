package com.dengas.devtimetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Ответ с токенами аутентификации")
public class TokenResponse {

    @Schema(description = "Access token для доступа к защищенным ресурсам", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String access_token;

    @Schema(description = "ID token, содержащий информацию о пользователе (если применимо)", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String id_token;

    @Schema(description = "Refresh token для обновления access token (если применимо)", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refresh_token;

    @Schema(description = "Время жизни access token в секундах", example = "3600")
    private Integer expires_in;

    public TokenResponse() {}

    public TokenResponse(String access_token, String id_token, String refresh_token, Integer expires_in) {
        this.access_token = access_token;
        this.id_token = id_token;
        this.refresh_token = refresh_token;
        this.expires_in = expires_in;
    }
}