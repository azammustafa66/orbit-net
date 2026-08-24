package com.orbitet.notificationservice.events;

import lombok.Data;

/**
 * Emitted by posts-service once per recipient when a post is created.
 * Mirrors {@code com.orbitet.events.PostCreated}; the two are bound by the
 * {@code postCreated} type mapping in application.yaml, not by package.
 */
@Data
public class PostCreated {
    /** The connection this notification is for. */
    private Long userId;
    private Long postId;
    private Long createdByUserId;
    private String contentSneakPeek;
}
