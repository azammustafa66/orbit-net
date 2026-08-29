package com.orbitet.utils;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/** Mirrors posts-service's error shape so clients see one response format across services. */
@Getter
public class ApiError {

    private final LocalDateTime timestamp;
    private final String error;
    private final HttpStatus statusCode;

    public ApiError(String error, HttpStatus statusCode) {
        this.timestamp = LocalDateTime.now();
        this.error = error;
        this.statusCode = statusCode;
    }
}
