package com.azam.orbitnet.user_service.exceptions;

public class BadCredentialsException extends RuntimeException {

    public BadCredentialsException(String message) {
        super(message);
    }
}
