package com.paulcera.bentabox.security.service;


import com.paulcera.bentabox.security.dto.LoginRequest;
import com.paulcera.bentabox.security.exception.InvalidRefreshTokenException;
import com.paulcera.bentabox.security.model.AuthenticationToken;
import com.paulcera.bentabox.security.model.UserPrincipal;
import com.paulcera.bentabox.security.model.WebUser;
import com.paulcera.bentabox.security.util.HttpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    @Autowired
    public AuthenticationService(AuthenticationManager authenticationManager, JWTService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthenticationToken authenticate(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        WebUser webUser = userPrincipal.webUser();

        String accessToken = jwtService.generateAccessToken(loginRequest.getUsername());
        String refreshToken = jwtService.generateRefreshToken(webUser);

        return new AuthenticationToken(accessToken, refreshToken);
    }

    @PreAuthorize("hasAuthority('USER')")
    public void initiateLogout(HttpServletRequest request) {
        String token = HttpUtil.extractAuthToken(request);

        jwtService.invalidateToken(token);
    }

    @PreAuthorize("hasAuthority('USER')")
    public AuthenticationToken issueNewToken(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String refreshToken = HttpUtil.extractAuthToken(request);

        boolean isRefreshTokenInvalid = !jwtService.isValidRefreshToken(refreshToken);
        if (isRefreshTokenInvalid) {
            throw new InvalidRefreshTokenException("RefreshToken is not valid.");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String newAccessToken = jwtService.generateAccessToken(userPrincipal.getUsername());

        return new AuthenticationToken(newAccessToken, refreshToken);
    }
}
