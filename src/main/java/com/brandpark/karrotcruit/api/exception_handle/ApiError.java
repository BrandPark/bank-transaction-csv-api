package com.brandpark.karrotcruit.api.exception_handle;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
public class ApiError {
    private HttpStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private String message;
    private String debugMessage;

    private ApiError() {
        timestamp = LocalDateTime.now();
    }

    public ApiError(HttpStatus status) {
        this();
        this.status = status;
    }

    public ApiError(HttpStatus status, Throwable cause) {
        this();
        this.status = status;
        this.message = "Unexpected error";
        this.debugMessage = cause.getLocalizedMessage();
    }

    public ApiError(HttpStatus status, String message, Throwable cause) {
        this();
        this.status = status;
        this.message = message;
        this.debugMessage = cause.getLocalizedMessage();
    }
}
