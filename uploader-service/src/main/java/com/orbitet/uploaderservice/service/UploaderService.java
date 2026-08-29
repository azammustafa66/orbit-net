package com.orbitet.uploaderservice.service;


import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/** Abstraction over the storage backend so callers depend on an upload contract, not on Cloudinary. */
public interface UploaderService {

    /** @return the secure (https) URL Cloudinary assigned the stored file - not a local path or id. */
    String upload(MultipartFile file) throws IOException;
}
