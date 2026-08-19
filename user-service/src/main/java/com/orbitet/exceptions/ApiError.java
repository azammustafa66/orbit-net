package com.azam.orbitnet.user_service.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class ApiError {

    private final LocalDateTime timestamp;
    private String error;
    private HttpStatus statusCode;
    private Map<String, String> fieldErrors;

    private ApiError() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiError(String error, HttpStatus statusCode) {
        this();
        this.error = error;
        this.statusCode = statusCode;
    }

    public ApiError(String error, HttpStatus statusCode, Map<String, String> fieldErrors) {
        this(error, statusCode);
        this.fieldErrors = fieldErrors;
    }
}
