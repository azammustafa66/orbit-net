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

    @GetMapping("/first-degree")
    public ResponseEntity<List<Person>> getFirstDegreeConnections(@RequestHeader("X-User-Id") Long userId) {
        List<Person> personList = connectionService.getFirstDegreeConnections(userId);
        return ResponseEntity.ok(personList);
    }
}
