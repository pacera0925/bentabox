package com.paulcera.bentabox.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.paulcera.bentabox.security.model.UserPrincipal;
import com.paulcera.bentabox.security.model.WebUser;
import com.paulcera.bentabox.security.model.WebUserMother;
import com.paulcera.bentabox.security.repository.WebUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private WebUserRepository webUserRepository;

    @Test
    void loadUserByUsername_notExistingWebUser_throwsException() {
        String username = WebUserMother.admin().getUsername();
        when(webUserRepository.findByUsername(username)).thenReturn(Optional.empty());

        UsernameNotFoundException thrown = assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(username),
            "Expected loadUserByUsername() to throw UsernameNotFoundException, but it didn't"
        );

        assertEquals("No user found with username: " + username, thrown.getMessage());

    }

    @Test
    void loadUserByUsername_existingWebUser_returnsUserPrincipal() {
        WebUser user = WebUserMother.admin();
        String username = user.getUsername();
        when(webUserRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserPrincipal result = (UserPrincipal) userDetailsService.loadUserByUsername(username);

        assertNotNull(result);
        assertEquals(user, result.webUser());
    }

}