package com.orbitet.client;

import com.orbitet.dto.PersonDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "connection-service", path = "/api/v1/connections", url = "${CONNECTION_SERVICE_URI:}")
public interface ConnectionServiceClient {

    /**
     * The caller's id travels in the {@code X-User-Id} header, which
     * {@link com.orbitet.auth.FeignClientInterceptor} adds to every outgoing request —
     * declaring it here as well would send the value twice.
     */
    @GetMapping("/first-degree")
    List<PersonDto> getFirstDegreeConnections();
}
