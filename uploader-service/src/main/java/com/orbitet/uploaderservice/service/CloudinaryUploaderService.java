package com.orbitet.uploaderservice.service;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Uploads files to Cloudinary using default upload options (no folder, transformation, or
 * resource-type override). Reading the multipart file into memory throws IOException on
 * I/O failure; the Cloudinary call itself throws an unchecked RuntimeException on API-level
 * failures (auth, quota, network), which is not caught here and propagates to the caller.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CloudinaryUploaderService implements UploaderService {

    private final Cloudinary cloudinary;

    @Override
    public String upload(MultipartFile file) throws IOException {
        log.info("Uploading file {} to Cloudinary", file.getOriginalFilename());
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), Map.of());
        return uploadResult.get("secure_url").toString();
    }
}
