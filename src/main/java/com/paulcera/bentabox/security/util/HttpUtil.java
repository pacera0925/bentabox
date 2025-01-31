package com.paulcera.bentabox.security.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

public final class HttpUtil {

    private HttpUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String extractAuthToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

}
