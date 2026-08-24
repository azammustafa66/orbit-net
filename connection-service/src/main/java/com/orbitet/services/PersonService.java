package com.orbitet.services;

import com.orbitet.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonService {

    private final PersonRepository personRepository;

    /**
     * Safe to call more than once for the same user — the underlying query merges on
     * {@code userId}, so a redelivered event updates the existing node instead of
     * adding a second one.
     */
    public void createPerson(Long userId, String name) {
        log.info("Creating person for user {}", userId);
        personRepository.upsertByUserId(userId, name);
    }
}
