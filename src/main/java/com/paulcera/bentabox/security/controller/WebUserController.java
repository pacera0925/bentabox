package com.paulcera.bentabox.security.controller;


import com.paulcera.bentabox.security.dto.WebUserForm;
import com.paulcera.bentabox.security.dto.WebUserView;
import com.paulcera.bentabox.security.service.WebUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class WebUserController {

    private final WebUserService webUserService;

    @Autowired
    public WebUserController(WebUserService webUserService) {
        this.webUserService = webUserService;
    }

    @PostMapping("/add")
    public ResponseEntity<WebUserView> createWebUser(@RequestBody WebUserForm form) {
        WebUserView newWebUser = webUserService.create(form);
        return new ResponseEntity<>(newWebUser, HttpStatus.CREATED);
    }
}
