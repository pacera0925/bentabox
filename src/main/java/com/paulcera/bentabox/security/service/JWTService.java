package com.paulcera.bentabox.security.service;

import com.paulcera.bentabox.security.model.WebUser;
import org.springframework.security.core.userdetails.UserDetails;

public interface JWTService {

    String generateAccessToken(String username);

    String generateRefreshToken(WebUser webUser);

    String extractUsername(String token);

    boolean isTokenValidForUser(String token, UserDetails userDetails);

    void invalidateToken(String token);

    boolean isValidRefreshToken(String refreshToken);
}
