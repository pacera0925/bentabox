package com.paulcera.bentabox.security.service;


import static com.paulcera.bentabox.security.util.UnitTestProps.accessTokenExpiration;
import static com.paulcera.bentabox.security.util.UnitTestProps.jwtSecretKey;
import static com.paulcera.bentabox.security.util.UnitTestProps.refreshTokenExpiration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paulcera.bentabox.security.exception.TokenNotFoundException;
import com.paulcera.bentabox.security.model.RefreshToken;
import com.paulcera.bentabox.security.model.RefreshTokenMother;
import com.paulcera.bentabox.security.model.UserPrincipalMother;
import com.paulcera.bentabox.security.model.WebUser;
import com.paulcera.bentabox.security.model.WebUserMother;
import com.paulcera.bentabox.security.repository.RefreshTokenRepository;
import com.paulcera.bentabox.security.util.JWTInput;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JWTServiceImplTest {

    private JWTServiceImpl jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setup() {
        jwtService = new JWTServiceImpl(jwtSecretKey(), accessTokenExpiration(), refreshTokenExpiration(),
            refreshTokenRepository);
    }

    @Test
    void generateAccessToken_withPassedUsername_returnsValidToken() {
        String username = "admin";
        String token = jwtService.generateAccessToken(username);

        assertNotNull(token);
        assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    void generateRefreshToken_withPassedWebUser_returnsAndSaveValidToken() {
        WebUser webUser = WebUserMother.admin();

        String result = jwtService.generateRefreshToken(webUser);

        assertNotNull(result);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        assertEquals(webUser.getUsername(), jwtService.extractUsername(result));
    }

    @Test
    void extractUsername_adminToken_returnsAdminUsername() {
        String username = "admin";
        String token = jwtService.generateAccessToken(username);

        assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    void isTokenValid_hasValidToken_returnsTrue() {
        String token = jwtService.generateAccessToken(WebUserMother.admin().getUsername());

        boolean result = jwtService.isTokenValidForUser(token, UserPrincipalMother.admin());

        assertTrue(result);
    }

    @Test
    void isTokenValid_tokenExpired_returnsFalse() {
        String expiredToken = JWTInput.expiredAdminToken();

        boolean result = jwtService.isTokenValidForUser(expiredToken, UserPrincipalMother.admin());

        assertFalse(result);
    }

    @Test
    void isTokenValid_tokenUsernameDifferent_returnsFalse() {
        String tokenWithIncorrectUsername = jwtService.generateAccessToken("adminx");

        boolean result = jwtService.isTokenValidForUser(tokenWithIncorrectUsername, UserPrincipalMother.admin());

        assertFalse(result);
    }

    @Test
    void invalidateToken_tokenNotExisting_throwsException() {
        String token = "token";
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        TokenNotFoundException thrown = assertThrows(
            TokenNotFoundException.class,
            () -> jwtService.invalidateToken(token),
            "Expected invalidateToken() to throw TokenNotFoundException, but it didn't"
        );

        assertEquals("No RefreshToken found with value: token", thrown.getMessage());
    }

    @Test
    void invalidateToken_tokenExists_revokeToken() {
        String token = "token";
        RefreshToken refreshToken = RefreshTokenMother.token();
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

        jwtService.invalidateToken("token");

        verify(refreshTokenRepository, times(1)).save(refreshToken);
        assertTrue(refreshToken.isRevoked());
    }

    @Test
    void isValidRefreshToken_expiredToken_returnsFalse() {
        String expiredToken = JWTInput.expiredAdminToken();
        when(refreshTokenRepository.existsByToken(expiredToken)).thenReturn(true);
        when(refreshTokenRepository.isTokenRevoked(expiredToken)).thenReturn(false);

        boolean result = jwtService.isValidRefreshToken(expiredToken);

        assertFalse(result);
    }

    @Test
    void isValidRefreshToken_tokenNotExisting_returnsFalse() {
        String validToken = jwtService.generateAccessToken(WebUserMother.admin().getUsername());
        when(refreshTokenRepository.existsByToken(validToken)).thenReturn(false);
        when(refreshTokenRepository.isTokenRevoked(validToken)).thenReturn(false);

        boolean result = jwtService.isValidRefreshToken(validToken);

        assertFalse(result);
    }

    @Test
    void isValidRefreshToken_tokenRevoked_returnsFalse() {
        String validToken = jwtService.generateAccessToken(WebUserMother.admin().getUsername());
        when(refreshTokenRepository.existsByToken(validToken)).thenReturn(true);
        when(refreshTokenRepository.isTokenRevoked(validToken)).thenReturn(true);

        boolean result = jwtService.isValidRefreshToken(validToken);

        assertFalse(result);
    }

    @Test
    void isValidRefreshToken_validToken_returnsTrue() {
        String validToken = jwtService.generateAccessToken(WebUserMother.admin().getUsername());
        when(refreshTokenRepository.existsByToken(validToken)).thenReturn(true);
        when(refreshTokenRepository.isTokenRevoked(validToken)).thenReturn(false);

        boolean result = jwtService.isValidRefreshToken(validToken);

        assertTrue(result);
    }

}