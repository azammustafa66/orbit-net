package com.orbitet.auth;

import com.orbitet.exceptions.UnauthorizedException;

/**
 * Per-request holder for the caller's id, populated by {@link RequestInterceptor} from the
 * gateway's {@code X-User-Id} header.
 */
public class AuthContextHolder {

    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    public static Long getCurrentUserId() {
        return currentUserId.get();
    }

    /**
     * The caller's id, for endpoints that cannot serve an anonymous request.
     *
     * @throws UnauthorizedException when no valid token reached the gateway
     */
    public static Long requireCurrentUserId() {
        Long userId = currentUserId.get();
        if (userId == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return userId;
    }

    static void setCurrentUserId(Long userId) {
        currentUserId.set(userId);
    }

    static void destroyCurrentUserId() {
        currentUserId.remove();
    }
}
