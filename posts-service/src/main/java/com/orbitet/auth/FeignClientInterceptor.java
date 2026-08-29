package com.orbitet.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

/**
 * Forwards the caller's id from {@link AuthContextHolder} onto outgoing Feign requests, so
 * downstream services see the same {@code X-User-Id} header the gateway sent.
 */
@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Long userId = AuthContextHolder.getCurrentUserId();
        if (userId != null) {
            template.header("X-User-Id", userId.toString());
        }
    }
}
