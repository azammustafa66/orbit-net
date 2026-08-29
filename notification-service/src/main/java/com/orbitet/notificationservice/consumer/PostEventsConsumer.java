package com.orbitet.notificationservice.consumer;

import com.orbitet.notificationservice.events.PostCreated;
import com.orbitet.notificationservice.events.PostLiked;
import com.orbitet.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Turns the events posts-service publishes into stored notifications.
 *
 * <p>Delivery is at-least-once, so a redelivered record produces a duplicate
 * notification. That is acceptable here; an event id plus a unique constraint
 * would be the fix if it stops being so.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PostEventsConsumer {

    private final NotificationService notificationService;

    /**
     * Listens on {@code post_created_topic}. posts-service fans out one message per
     * connection, keyed by the recipient's user id, so all of one user's
     * post-created notifications land in the same partition and stay ordered.
     */
    @KafkaListener(topics = "post_created_topic")
    public void handlePostCreated(@NonNull PostCreated event) {
        log.info("Received post_created event: {}", event.toString());
        notificationService.notifyUser(
                event.getUserId(),
                "User %d posted: %s".formatted(event.getCreatedByUserId(), event.getContentSneakPeek()));
    }

    /**
     * Listens on {@code post_liked_topic}, keyed by the post owner's (recipient's)
     * user id so one user's like notifications stay ordered within a partition.
     */
    @KafkaListener(topics = "post_liked_topic")
    public void handlePostLiked(PostLiked event) {
        log.info("Received post_liked event: {}", event);
        notificationService.notifyUser(
                event.getOwnerUserId(),
                "User %d liked your post %d".formatted(event.getLikedByUserId(), event.getPostId()));
    }
}
