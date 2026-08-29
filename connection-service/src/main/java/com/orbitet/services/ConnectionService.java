package com.orbitet.services;

import com.orbitet.auth.AuthContextHolder;
import com.orbitet.entities.Person;
import com.orbitet.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Guards the connection-request state machine ({@code REQUESTED_TO} &rarr;
 * {@code CONNECTED_TO}) so callers can't re-request or double-accept.
 */
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
        Long fromUserId = AuthContextHolder.requireCurrentUserId();
        log.info("Sending connection request from {} for user {}", fromUserId, toUserId);

        // Either state on its own already blocks a new request; requiring both would let a
        // caller re-request someone they had only requested, duplicating the REQUESTED_TO edge.
        boolean connectionRequestExists = personRepository.connectionRequestExists(fromUserId, toUserId);
        boolean alreadyConnected = personRepository.alreadyConnected(fromUserId, toUserId);
        if (connectionRequestExists || alreadyConnected) {
            throw new RuntimeException("Connection already requested or already connected");
        }

        personRepository.addConnectionRequest(fromUserId, toUserId);
    }

    /**
     * @param fromUserId the user who raised the request — the source of the
     *                   {@code REQUESTED_TO} edge, not the caller accepting it
     */
    public void acceptConnectionRequest(Long fromUserId, Long toUserId) {
        log.info("Accepting connection request from {} for user {}", fromUserId, toUserId);
        boolean connectionRequestExists = personRepository.connectionRequestExists(fromUserId, toUserId);
        boolean alreadyConnected = personRepository.alreadyConnected(fromUserId, toUserId);
        // Unlike a send, accepting *needs* the pending request to exist — the repository
        // query is a silent no-op otherwise, so an absent edge has to fail loudly here.
        if (!connectionRequestExists) {
            throw new RuntimeException("No pending connection request to accept");
        }
        if (alreadyConnected) {
            throw new RuntimeException("Already connected");
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
