package com.dengas.devtimetracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Стандартна відповідь API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyApiResponse<T> {

    @Schema(description = "Дані відповіді", nullable = true)
    private T data;

    @Schema(description = "Повідомлення про помилку", nullable = true, example = "Error message")
    private String error;

    @Schema(description = "Час створення відповіді", example = "2024-01-15T10:30:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;

    // Конструктор для успішної відповіді
    public MyApiResponse(T data) {
        this.data = data;
        this.timestamp = Instant.now();
        this.error = null;
    }

    // Конструктор для помилки
    public MyApiResponse(String error) {
        this.data = null;
        this.timestamp = Instant.now();
        this.error = error;
    }

    // Статичні методи для зручності
    public static <T> MyApiResponse<T> success(T data) {
        return new MyApiResponse<>(data);
    }

    public static <T> MyApiResponse<T> error(String errorMessage) {
        return new MyApiResponse<>(errorMessage);
    }

    // Геттери
    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    // Сеттери (для Jackson)
    public void setData(T data) {
        this.data = data;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}