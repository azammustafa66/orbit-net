package com.orbitet.notificationservice.consumer;

import com.orbitet.notificationservice.events.PostCreated;
import com.orbitet.notificationservice.events.PostLiked;
import com.orbitet.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @KafkaListener(topics = "post_created_topic")
    public void handlePostCreated(PostCreated event) {
        log.info("Received post_created event: {}", event);
        notificationService.notifyUser(
                event.getUserId(),
                "User %d posted: %s".formatted(event.getCreatedByUserId(), event.getContentSneakPeek()));
    }

    @KafkaListener(topics = "post_liked_topic")
    public void handlePostLiked(PostLiked event) {
        log.info("Received post_liked event: {}", event);
        notificationService.notifyUser(
                event.getOwnerUserId(),
                "User %d liked your post %d".formatted(event.getLikedByUserId(), event.getPostId()));
    }
}
