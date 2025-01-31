package com.paulcera.bentabox.security.service;

import com.paulcera.bentabox.security.model.UserPrincipal;
import com.paulcera.bentabox.security.model.WebUser;
import com.paulcera.bentabox.security.repository.WebUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final WebUserRepository webUserRepository;

    @Autowired
    public UserDetailsServiceImpl(WebUserRepository webUserRepository) {
        this.webUserRepository = webUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        WebUser user = webUserRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + username));

        return new UserPrincipal(user);
    }
}
