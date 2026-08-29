package com.orbitet.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Populates {@link AuthContextHolder} from the {@code X-User-Id} header the gateway sets
 * on an authenticated request, and clears it once the request completes.
 */
@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // The gateway only sets this header for a request carrying a valid token, so an
        // absent or unparseable value simply means the caller is anonymous.
        String userId = request.getHeader("X-User-Id");
        if (userId != null && !userId.isBlank()) {
            try {
                AuthContextHolder.setCurrentUserId(Long.valueOf(userId));
            } catch (NumberFormatException ex) {
            }
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) throws Exception {
        AuthContextHolder.destroyCurrentUserId();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
