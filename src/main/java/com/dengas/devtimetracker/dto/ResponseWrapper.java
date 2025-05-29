package com.dengas.devtimetracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseWrapper<T> {
    private boolean success;
    private T data;
    private ErrorDetails error;
    private LocalDateTime timestamp;
    private int status;

    public ResponseWrapper() {
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ResponseWrapper<T> success(T data) {
        ResponseWrapper<T> response = new ResponseWrapper<>();
        response.setSuccess(true);
        response.setData(data);
        response.setStatus(HttpStatus.OK.value());
        return response;
    }

    public static <T> ResponseWrapper<T> error(HttpStatus status, String message, String code) {
        ResponseWrapper<T> response = new ResponseWrapper<>();
        response.setSuccess(false);
        response.setStatus(status.value());
        response.setError(new ErrorDetails(message, code));
        return response;
    }

    @Data
    public static class ErrorDetails {
        private String message;
        private String code;

        public ErrorDetails(String message, String code) {
            this.message = message;
            this.code = code;
        }
    }
}