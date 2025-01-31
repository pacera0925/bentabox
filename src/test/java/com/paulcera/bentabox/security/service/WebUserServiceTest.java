package com.paulcera.bentabox.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paulcera.bentabox.security.dto.WebUserForm;
import com.paulcera.bentabox.security.dto.WebUserFormMother;
import com.paulcera.bentabox.security.dto.WebUserView;
import com.paulcera.bentabox.security.model.WebUser;
import com.paulcera.bentabox.security.model.WebUserMother;
import com.paulcera.bentabox.security.repository.WebUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class WebUserServiceTest {

    @InjectMocks
    private WebUserService webUserService;

    @Mock
    private WebUserRepository webUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final ArgumentCaptor<WebUser> webUserCaptor = ArgumentCaptor.forClass(WebUser.class);

    @Test
    void create_validForm_success() {
        WebUserForm form = WebUserFormMother.joe();
        WebUser joe = WebUserMother.joe();
        when(passwordEncoder.encode(form.getPassword())).thenReturn("encoded-pass");
        when(webUserRepository.save(any(WebUser.class))).thenReturn(joe);

        WebUserView result = webUserService.create(form);

        verify(webUserRepository, times(1)).save(webUserCaptor.capture());
        assertNotNull(result);
        assertEquals(joe.getUsername(), result.getUsername());
        WebUser createdWebUser = webUserCaptor.getValue();
        assertEquals(joe.getUsername(), createdWebUser.getUsername());
    }

}