package com.paulcera.bentabox.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paulcera.bentabox.security.dto.LoginRequest;
import com.paulcera.bentabox.security.dto.LoginRequestMother;
import com.paulcera.bentabox.security.exception.InvalidRefreshTokenException;
import com.paulcera.bentabox.security.model.AuthenticationToken;
import com.paulcera.bentabox.security.model.UserPrincipal;
import com.paulcera.bentabox.security.model.UserPrincipalMother;
import com.paulcera.bentabox.security.util.HttpServletRequestMother;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwtService;

    @Test
    void authenticate_hasValidCredentials_success() {
        LoginRequest loginRequest = LoginRequestMother.admin();
        Authentication mockedAuthentication = mock(Authentication.class);
        UserPrincipal userPrincipal = UserPrincipalMother.admin();
        when(mockedAuthentication.getPrincipal()).thenReturn(userPrincipal);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockedAuthentication);
        when(jwtService.generateAccessToken(loginRequest.getUsername())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(userPrincipal.webUser())).thenReturn("refresh-token");

        AuthenticationToken result = authenticationService.authenticate(loginRequest);

        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
    }

    @Test
    void authenticate_hasInvalidCredentials_throwsException() {
        LoginRequest loginRequest = LoginRequestMother.admin();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        BadCredentialsException thrown = assertThrows(
            BadCredentialsException.class,
            () -> authenticationService.authenticate(loginRequest),
            "Expected authenticate() to throw BadCredentialsException, but it didn't"
        );

        assertEquals("Bad credentials", thrown.getMessage());
    }

    @Test
    void initiateLogout_callsExpectedService() {
        String myToken = "jwt-token";
        HttpServletRequest request = HttpServletRequestMother.withBearerToken(myToken);

        authenticationService.initiateLogout(request);

        verify(jwtService, times(1)).invalidateToken(myToken);
    }

    @Test
    void issueNewToken_hasValidRefreshToken_success() {
        try (MockedStatic<SecurityContextHolder> mockedContextHolder = mockStatic(SecurityContextHolder.class)){
            String myRefreshToken = "refresh-jwt";
            String newAccessToken = "access-jwt-new";
            HttpServletRequest request = HttpServletRequestMother.withBearerToken(myRefreshToken);
            SecurityContext mockedSecurityContext = mock(SecurityContext.class);
            Authentication mockedAuthentication = mock(Authentication.class);
            mockedContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockedSecurityContext);
            when(mockedSecurityContext.getAuthentication()).thenReturn(mockedAuthentication);

            UserPrincipal admin = UserPrincipalMother.admin();
            when(mockedAuthentication.getPrincipal()).thenReturn(admin);

            when(jwtService.isValidRefreshToken(myRefreshToken)).thenReturn(true);
            when(jwtService.generateAccessToken(admin.getUsername())).thenReturn(newAccessToken);

            AuthenticationToken result = authenticationService.issueNewToken(request);

            assertEquals(newAccessToken, result.accessToken());
            assertEquals(myRefreshToken, result.refreshToken());
        }
    }

    @Test
    void issueNewToken_hasInvalidRefreshToken_throwsException() {
        String myToken = "jwt-token";
        HttpServletRequest request = HttpServletRequestMother.withBearerToken(myToken);

        when(jwtService.isValidRefreshToken(myToken)).thenReturn(false);

        InvalidRefreshTokenException thrown = assertThrows(
            InvalidRefreshTokenException.class,
            () -> authenticationService.issueNewToken(request),
            "Expected authenticate() to throw InvalidCredentialsException, but it didn't"
        );

        assertEquals("RefreshToken is not valid.", thrown.getMessage());
    }
}