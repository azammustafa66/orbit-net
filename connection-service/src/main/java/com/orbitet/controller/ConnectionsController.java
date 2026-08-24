package com.orbitet.controller;

import com.orbitet.auth.AuthContextHolder;
import com.orbitet.entities.Person;
import com.orbitet.services.ConnectionService;
import com.orbitet.services.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class ConnectionsController {

    private final ConnectionService connectionService;

    @GetMapping("/first-degree")
    public ResponseEntity<List<Person>> getFirstDegreeConnections(@RequestHeader("X-User-Id") Long userId) {
        List<Person> personList = connectionService.getFirstDegreeConnections(userId);
        return ResponseEntity.ok(personList);
    }

    @PostMapping("/request/{userId}")
    public ResponseEntity<Void> sendConnectionRequest(@PathVariable("userId") Long userId) {
        connectionService.sendConnectionRequest(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/request/accept-connection/{userId}")
    public ResponseEntity<Void> acceptConnectionRequest(@PathVariable("userId") Long fromUserId) {
        connectionService.acceptConnectionRequest(AuthContextHolder.getCurrentUserId(), fromUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/request/reject-connection/{userId}")
    public ResponseEntity<Void> rejectConnectionRequest(@PathVariable("userId") Long fromUserId) {
        connectionService.rejectConnectionRequest(fromUserId,  AuthContextHolder.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
