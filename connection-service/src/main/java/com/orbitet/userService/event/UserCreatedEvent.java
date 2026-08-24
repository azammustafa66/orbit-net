package com.orbitet.userService.event;

import lombok.Data;

/**
 * Emitted by user-service when an account is created.
 * Mirrors {@code com.orbitet.event.UserCreatedEvent}; the two are bound by the
 * {@code userCreated} type mapping in application.yaml, not by package.
 * <p>
 * No {@code @Builder} here on purpose: it would suppress the no-args constructor
 * Jackson needs to deserialize, and nothing on this side ever builds the event.
 */
@Data
public class UserCreatedEvent {

    private Long userId;
    private String name;
}
