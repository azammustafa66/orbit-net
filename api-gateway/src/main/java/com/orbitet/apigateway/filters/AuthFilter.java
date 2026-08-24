package com.orbitet.apigateway.filters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.*;

/**
 * Extracts the user id from the bearer token and passes it downstream as {@code X-User-Id}.
 * Requests without a valid token are forwarded unchanged.
 */
@Component
public class AuthFilter extends OncePerRequestFilter {

    private static final String USER_ID = "X-User-Id";
    private static final String USER_EMAIL = "X-User-Email";
    private static final String BEARER = "Bearer ";
    private static final Set<String> GATEWAY_HEADERS = gatewayHeaders();

    private final SecretKey key;

    public AuthFilter(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        Map<String, String> resolved = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (auth != null && auth.startsWith(BEARER)) {
            try {
                Claims claims = Jwts.parser().verifyWith(key).build()
                        .parseSignedClaims(auth.substring(BEARER.length()).trim())
                        .getPayload();
                if (claims.getSubject() != null) {
                    resolved.put(USER_ID, claims.getSubject());
                    String email = claims.get("email", String.class);
                    if (email != null) {
                        resolved.put(USER_EMAIL, email);
                    }
                }
            } catch (JwtException | IllegalArgumentException ex) {
                // Bad or expired token: `resolved` stays empty, so the headers are stripped.
            }
        }

        filterChain.doFilter(withUserHeaders(request, resolved), response);
    }

    private HttpServletRequest withUserHeaders(HttpServletRequest request, Map<String, String> resolved) {
        return new HttpServletRequestWrapper(request) {

            @Override
            public String getHeader(String name) {
                return GATEWAY_HEADERS.contains(name) ? resolved.get(name) : super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (!GATEWAY_HEADERS.contains(name)) {
                    return super.getHeaders(name);
                }
                String value = resolved.get(name);
                return value != null ? Collections.enumeration(List.of(value))
                        : Collections.emptyEnumeration();
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> names = Collections.list(super.getHeaderNames());
                names.removeIf(GATEWAY_HEADERS::contains);
                names.addAll(resolved.keySet());
                return Collections.enumeration(names);
            }
        };
    }

    private static Set<String> gatewayHeaders() {
        Set<String> headers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        headers.add(USER_ID);
        headers.add(USER_EMAIL);
        return Collections.unmodifiableSet(headers);
    }
}
