package com.orbitet.controller;

import com.orbitet.auth.AuthContextHolder;
import com.orbitet.entities.Person;
import com.orbitet.services.ConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * A connection request survives as {@code REQUESTED_TO} until the recipient accepts
 * (promoting it to {@code CONNECTED_TO}) or rejects it (deleting it).
 */
@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class ConnectionsController {

    private final ConnectionService connectionService;

    @GetMapping("/first-degree")
    public ResponseEntity<List<Person>> getFirstDegreeConnections() {
        List<Person> personList =
                connectionService.getFirstDegreeConnections(AuthContextHolder.requireCurrentUserId());
        return ResponseEntity.ok(personList);
    }

    @PostMapping("/request/{userId}")
    public ResponseEntity<Void> sendConnectionRequest(@PathVariable("userId") Long userId) {
        connectionService.sendConnectionRequest(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * The path variable is the user who raised the request, so the caller is the receiver
     * of the {@code REQUESTED_TO} edge — passing the pair the other way round matches no
     * edge and silently does nothing.
     */
    @PostMapping("/request/accept-connection/{userId}")
    public ResponseEntity<Void> acceptConnectionRequest(@PathVariable("userId") Long fromUserId) {
        connectionService.acceptConnectionRequest(fromUserId, AuthContextHolder.requireCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/request/reject-connection/{userId}")
    public ResponseEntity<Void> rejectConnectionRequest(@PathVariable("userId") Long fromUserId) {
        connectionService.rejectConnectionRequest(fromUserId, AuthContextHolder.requireCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
