package com.orbitet.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "uploader-service", path = "/api/v1/file")
public interface UploaderServiceClient {

    /**
     * {@code consumes} is what routes the call through Feign's form encoder — without it
     * the default encoder tries to serialise the {@link MultipartFile} as JSON and fails.
     *
     * @return the Cloudinary URL of the stored image
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> upload(@RequestPart("file") MultipartFile file);
}
