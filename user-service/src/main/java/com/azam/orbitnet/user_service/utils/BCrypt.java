package com.azam.orbitnet.user_service.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password hashing for user-service.
 *
 * <p>Backed by Spring Security's {@link BCryptPasswordEncoder}, which generates a fresh salt
 * per call and embeds it in the returned hash — so verification needs only the stored hash.
 * The encoder is stateless and thread-safe, hence the single shared instance.
 */
public final class BCrypt {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private BCrypt() {
    }

    /**
     * @return a salted BCrypt hash, safe to store as-is
     */
    public static String hash(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * @param storedHash a hash previously produced by {@link #hash(String)}
     * @return true when the raw password matches the stored hash
     */
    public static boolean verify(String rawPassword, String storedHash) {
        return ENCODER.matches(rawPassword, storedHash);
    }
}
