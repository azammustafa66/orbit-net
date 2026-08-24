package com.orbitet.consumer;

import com.orbitet.services.PersonService;
import com.orbitet.userService.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceConsumer {

    private final PersonService personService;

    @KafkaListener(topics = "user_created_topic")
    public void handlePersonCreated(UserCreatedEvent userCreatedEvent) {
        log.info("Received UserCreatedEvent {}", userCreatedEvent);
        personService.createPerson(userCreatedEvent.getUserId(), userCreatedEvent.getName());
    }

}
