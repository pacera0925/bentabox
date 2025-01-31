package com.paulcera.bentabox.security.service;

import com.paulcera.bentabox.security.dto.WebUserForm;
import com.paulcera.bentabox.security.dto.WebUserView;
import com.paulcera.bentabox.security.model.WebUser;
import com.paulcera.bentabox.security.repository.WebUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class WebUserService {

    private final WebUserRepository webUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public WebUserService(WebUserRepository webUserRepository, PasswordEncoder passwordEncoder) {
        this.webUserRepository = webUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public WebUserView create(WebUserForm form) {
        WebUser newWebUser = webUserRepository.save(WebUser.createFromForm(form, passwordEncoder));
        return new WebUserView(newWebUser);
    }

}
