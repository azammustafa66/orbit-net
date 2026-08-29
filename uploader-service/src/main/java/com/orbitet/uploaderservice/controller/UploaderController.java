package com.orbitet.uploaderservice.controller;

import com.orbitet.uploaderservice.service.UploaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/** Exposed at /api/v1/file (context-path + mapping) since Feign callers reach this service directly via Eureka. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class UploaderController {

    private final UploaderService uploaderService;

    /** Response body is the bare Cloudinary URL string, not a JSON envelope. */
    @PostMapping()
    public ResponseEntity<String> upload(@RequestParam MultipartFile file) throws IOException {
        String url = uploaderService.upload(file);
        return ResponseEntity.ok(url);
    }
}
