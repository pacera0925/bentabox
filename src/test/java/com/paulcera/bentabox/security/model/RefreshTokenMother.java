package com.paulcera.bentabox.security.model;

import java.time.Instant;

public class RefreshTokenMother {

    public static RefreshToken token() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1);
        refreshToken.setToken("refreshToken");
        refreshToken.setExpiryDate(Instant.now());
        refreshToken.setCreatedDate(Instant.now());
        return refreshToken;
    }

}
