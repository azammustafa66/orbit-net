package com.orbitet.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Puts {@link UserCreatedEvent} on the wire once the signup transaction has committed.
 * <p>
 * Sending from inside the transaction would announce a user that a later rollback erases,
 * leaving connection-service holding a Person node for an account that never existed.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedEventPublisher {

    private static final String TOPIC = "user_created_topic";

    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserCreated(UserCreatedEvent event) {
        log.info("Publishing UserCreatedEvent for user {}", event.getUserId());
        kafkaTemplate.send(TOPIC, event)
                // The commit already happened, so a failure here cannot be undone — it can
                // only be reported, or the user silently never reaches the connection graph.
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish UserCreatedEvent for user {}", event.getUserId(), ex);
                    }
                });
    }
}
