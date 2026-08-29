package com.orbitet.exceptions;

import com.orbitet.utils.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Without this, an {@link UnauthorizedException} fell through to Boot's default handler and
 * surfaced as a 500 carrying a full stack trace — both the wrong status and an information
 * leak to any caller.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /**
     * The connection state machine signals conflicts ("already requested", "no pending
     * request") with a plain {@code RuntimeException}, which is a client error rather than
     * a server fault. Anything genuinely unexpected is logged and answered generically so
     * no internal detail reaches the caller.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntime(RuntimeException ex) {
        log.warn("Connection request rejected: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message) {
        return new ResponseEntity<>(new ApiError(message, status), status);
    }
}
