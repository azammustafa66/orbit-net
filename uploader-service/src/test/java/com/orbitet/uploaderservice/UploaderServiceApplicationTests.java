package com.orbitet.uploaderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// The cloudinary.* properties have no defaults in application.yaml — the service is meant
// to fail startup without real credentials — and Maven does not read .env, so the context
// needs throwaway values here. Set inline rather than in a test application.yaml, which
// would shadow the main one and drop the rest of the config.
@SpringBootTest(properties = {
        "cloudinary.cloud-name=test-cloud",
        "cloudinary.api-key=test-key",
        "cloudinary.api-secret=test-secret"
})
class UploaderServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
