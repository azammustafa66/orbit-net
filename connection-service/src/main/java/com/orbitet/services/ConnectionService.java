package com.orbitet.services;

import com.orbitet.auth.AuthContextHolder;
import com.orbitet.entities.Person;
import com.orbitet.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectionService {

    private final PersonRepository personRepository;

    public List<Person> getFirstDegreeConnections(Long userId) {
        log.info("Getting first degree connections for user {}", userId);
        return personRepository.getFirstDegreeConnections(userId);
    }

    public void sendConnectionRequest(Long toUserId) {
        Long fromUserId = AuthContextHolder.getCurrentUserId();
        log.info("Sending connection request from {} for user {}", fromUserId, toUserId);

        boolean connectionRequestExists = personRepository.connectionRequestExists(fromUserId, toUserId);
        boolean alreadyConnected = personRepository.alreadyConnected(fromUserId, toUserId);
        if (connectionRequestExists && alreadyConnected) {
            throw new RuntimeException("Connection already requested or already connected");
        }

        personRepository.addConnectionRequest(fromUserId, toUserId);
    }

    public void acceptConnectionRequest(Long fromUserId, Long toUserId) {
        log.info("Accepting connection request from {} for user {}", fromUserId, toUserId);
        boolean connectionRequestExists = personRepository.connectionRequestExists(fromUserId, toUserId);
        boolean alreadyConnected = personRepository.alreadyConnected(fromUserId, toUserId);
        if (connectionRequestExists && alreadyConnected) {
            throw new RuntimeException("Connection already requested or already connected");
        }
        personRepository.acceptConnectionRequest(fromUserId, toUserId);
    }

    public void rejectConnectionRequest(Long fromUserId, Long toUserId) {
        log.info("Rejecting connection request from {} for user {}", fromUserId, toUserId);
        boolean connectionRequestExists = personRepository.connectionRequestExists(fromUserId, toUserId);
        boolean alreadyConnected = personRepository.alreadyConnected(fromUserId, toUserId);
        if (!connectionRequestExists && !alreadyConnected) {
            throw new RuntimeException("Can't reject connection request");
        }
        personRepository.rejectConnectionRequest(fromUserId, toUserId);
    }
}
