package com.dengas.devtimetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос для обмена авторизационного кода на токены")
public class CodeExchangeRequest {

    @Schema(description = "Авторизационный код от Keycloak", example = "abc123", required = true)
    private String code;

    @Schema(description = "URI для редиректа", example = "http://localhost:5173/callback", required = true)
    private String redirectUri;

    public CodeExchangeRequest() {}

    public CodeExchangeRequest(String code, String redirectUri) {
        this.code = code;
        this.redirectUri = redirectUri;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
}
