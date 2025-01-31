package com.paulcera.bentabox.security.repository;

import com.paulcera.bentabox.security.model.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);

    boolean existsByToken(String refreshToken);

    @Query("SELECT (count(token) > 0) FROM RefreshToken token WHERE token.token = :refreshToken AND token.revokedDate IS NOT NULL")
    boolean isTokenRevoked(String refreshToken);

}
