package com.paulcera.bentabox.security.controller;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paulcera.bentabox.BaseIntegrationTestController;
import com.paulcera.bentabox.security.dto.LoginRequest;
import com.paulcera.bentabox.security.dto.LoginRequestMother;
import com.paulcera.bentabox.security.filter.JWTFilter;
import com.paulcera.bentabox.security.model.RefreshToken;
import com.paulcera.bentabox.security.model.WebUser;
import com.paulcera.bentabox.security.model.WebUserMother;
import com.paulcera.bentabox.security.repository.RefreshTokenRepository;
import com.paulcera.bentabox.security.repository.WebUserRepository;
import com.paulcera.bentabox.security.service.JWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@Sql(scripts = "/authentication-controller-dataset.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class AuthenticationControllerIntegrationTest extends BaseIntegrationTestController {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private WebUserRepository webUserRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .addFilter(new JWTFilter(jwtService, userDetailsService))
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();

        refreshTokenRepository.deleteAll();
    }

    @Test
    void login_validCredentials_successMessage() throws Exception {
        LoginRequest loginRequest = LoginRequestMother.admin();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Successfully logged in."))
            .andExpect(jsonPath("$.payload.access_token").isNotEmpty())
            .andExpect(jsonPath("$.payload.refresh_token").isNotEmpty());
    }

    @Test
    void login_invalidCredentials_badCredentialsError() throws Exception {
        LoginRequest loginRequest = LoginRequestMother.adminIncorrect();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Bad credentials"))
            .andExpect(jsonPath("$.payload").isEmpty());
    }

    @Test
    void login_hasValidAuthBearerToken_alreadyLoggedInError() throws Exception {
        LoginRequest loginRequest = LoginRequestMother.admin();
        String token = "Bearer " + jwtService.generateAccessToken(loginRequest.getUsername());

        mockMvc.perform(post("/api/auth/login")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Already logged in."))
            .andExpect(jsonPath("$.payload").isEmpty());
    }

    @Test
    void logout_notExistingRefreshToken_tokenNotFoundError() throws Exception {
        String token = "Bearer " + jwtService.generateAccessToken(WebUserMother.admin().getUsername());

        mockMvc.perform(post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", startsWith("No RefreshToken found with value: ")))
            .andExpect(jsonPath("$.payload").isEmpty());
    }

    @Test
    void logout_validRefreshToken_success() throws Exception {
        WebUser webUser = webUserRepository.findById(1)
            .orElseThrow(() -> new IllegalStateException("Expected dataset should contain this web_user"));
        String refreshToken = jwtService.generateRefreshToken(webUser);
        String bearerToken = "Bearer " + refreshToken;

        mockMvc.perform(post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Successfully logged out."))
            .andExpect(jsonPath("$.payload").isEmpty());

        RefreshToken updatedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new IllegalStateException("Expected dataset should contain this refresh_token"));

        assertTrue(updatedRefreshToken.isRevoked());
    }

    @Test
    void refresh_invalidRefreshToken_invalidRefreshTokenError() throws Exception {
        String bearerToken = "Bearer " + jwtService.generateAccessToken(WebUserMother.admin().getUsername());

        mockMvc.perform(post("/api/auth/refresh")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("RefreshToken is not valid."))
            .andExpect(jsonPath("$.payload").isEmpty());
    }

    @Test
    void refresh_validRefreshToken_success() throws Exception {
        WebUser webUser = webUserRepository.findById(1)
            .orElseThrow(() -> new IllegalStateException("Expected dataset should contain this web_user"));
        String refreshToken = jwtService.generateRefreshToken(webUser);
        String bearerToken = "Bearer " + refreshToken;

        mockMvc.perform(post("/api/auth/refresh")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("New token issued."))
            .andExpect(jsonPath("$.payload.access_token").isNotEmpty())
            .andExpect(jsonPath("$.payload.refresh_token").isNotEmpty());
    }

}