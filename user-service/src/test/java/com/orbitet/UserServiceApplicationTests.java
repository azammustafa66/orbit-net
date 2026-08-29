package com.orbitet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// jwt.secret has no default in application.yaml — a missing JWT_SECRET is meant to fail
// startup — so the context needs a throwaway key here. Set inline rather than in a test
// application.yaml, which would shadow the main one and drop the rest of the config.
@SpringBootTest(properties = "jwt.secret=dGVzdC1vbmx5LW5vdC1hLXJlYWwtc2VjcmV0LWtleSE=")
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
