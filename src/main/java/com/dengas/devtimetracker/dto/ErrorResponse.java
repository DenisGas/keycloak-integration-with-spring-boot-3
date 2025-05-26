package com.dengas.devtimetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с ошибкой")
public class ErrorResponse {

    @Schema(description = "Описание ошибки", example = "Invalid username or password")
    private String error;

    public ErrorResponse() {}

    public ErrorResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
