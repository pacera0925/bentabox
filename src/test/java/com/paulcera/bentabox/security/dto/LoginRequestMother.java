package com.paulcera.bentabox.security.dto;

public class LoginRequestMother {

    public static LoginRequest admin() {
        return new LoginRequest("admin", "admin");
    }

    public static LoginRequest adminIncorrect() {
        return new LoginRequest("admin", "adminx");
    }

}
