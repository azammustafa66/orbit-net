package com.orbitet.client;

import com.orbitet.dto.PersonDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "connections-service", path = "/connections")
@Component
public interface ConnectionServiceClient {

    @GetMapping("/{userId}/first-degree")
    List<PersonDto> getFirstDegreeConnections(@RequestHeader("X-User-Id") Long userIdFromHeader);
}
