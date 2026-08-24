package com.orbitet.notificationservice.events;

import lombok.Data;

/**
 * Emitted by posts-service when a post is liked.
 * Mirrors {@code com.orbitet.events.PostLiked}; the two are bound by the
 * {@code postLiked} type mapping in application.yaml, not by package.
 */
@Data
public class PostLiked {
    /** The post's author — the one being notified. */
    private Long ownerUserId;
    private Long postId;
    private Long likedByUserId;
}
