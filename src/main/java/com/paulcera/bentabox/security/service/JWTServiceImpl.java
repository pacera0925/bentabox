package com.paulcera.bentabox.security.service;


import com.paulcera.bentabox.security.exception.TokenNotFoundException;
import com.paulcera.bentabox.security.model.RefreshToken;
import com.paulcera.bentabox.security.model.WebUser;
import com.paulcera.bentabox.security.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JWTServiceImpl implements JWTService {

    private final String secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public JWTServiceImpl(@Value("${spring.application.security.jwt.secret-key}") String secretKey,
        @Value("${spring.application.security.jwt.access-token-expiration}") long accessTokenExpiration,
        @Value("${spring.application.security.jwt.refresh-token-expiration}") long refreshTokenExpiration,
        RefreshTokenRepository refreshTokenRepository) {
        this.secretKey = secretKey;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public String generateAccessToken(String username) {
        return generateToken(username, accessTokenExpiration);
    }

    @Override
    public String generateRefreshToken(WebUser webUser) {
        String username = webUser.getUsername();
        String token = generateToken(username, refreshTokenExpiration);

        refreshTokenRepository.save(new RefreshToken(token, webUser, refreshTokenExpiration));

        return token;
    }

    private String generateToken(String username, long expiration) {
        return Jwts
            .builder()
            .subject(username)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getKey())
            .compact();
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    @Override
    public boolean isTokenValidForUser(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);

            return (username.equals(userDetails.getUsername()));
        } catch (ExpiredJwtException e) {
            return false;
        }
    }

    @Override
    public void invalidateToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new TokenNotFoundException("No RefreshToken found with value: " + token));

        refreshToken.invalidate();

        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public boolean isValidRefreshToken(String refreshToken) {
        boolean isTokenExisting = refreshTokenRepository.existsByToken(refreshToken);
        boolean isTokenNotRevoked = !refreshTokenRepository.isTokenRevoked(refreshToken);

        return isTokenValid(refreshToken) && isTokenExisting && isTokenNotRevoked;
    }

    private boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
