package com.orbitet.controller;

import com.orbitet.entities.Person;
import com.orbitet.services.ConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class ConnectionsController {

    private final ConnectionService connectionService;

    @GetMapping("/{userId}/first-degree")
    public ResponseEntity<List<Person>> getFirstDegreeConnections(@PathVariable Long userId, @RequestHeader("X-User-Id") Long userIdFromHeader) {
        List<Person> personList = connectionService.getFirstDegreeConnections(userId);
        return ResponseEntity.ok(personList);
    }
}
