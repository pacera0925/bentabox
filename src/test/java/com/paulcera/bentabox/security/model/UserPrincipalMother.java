package com.paulcera.bentabox.security.model;

public class UserPrincipalMother {

    public static UserPrincipal admin() {
        return new UserPrincipal(WebUserMother.admin());
    }

}
