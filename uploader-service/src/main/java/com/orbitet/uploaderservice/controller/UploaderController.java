package com.orbitet.uploaderservice.controller;

import com.orbitet.uploaderservice.service.UploaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class UploaderController {

    private final UploaderService uploaderService;

    @PostMapping()
    public ResponseEntity<String> upload(@RequestParam MultipartFile file) throws IOException {
        String url = uploaderService.upload(file);
        return ResponseEntity.ok(url);
    }
}
