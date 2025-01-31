package com.paulcera.bentabox.security.model;

public class WebUserMother {

    public static WebUser admin() {
        WebUser webUser = new WebUser();
        webUser.setId(1);
        webUser.setUsername("admin");
        webUser.setPassword("admin");
        webUser.setFullName(new FullName("PB", "Admin"));
        return webUser;
    }

    public static WebUser joe() {
        WebUser webUser = new WebUser();
        webUser.setId(2);
        webUser.setUsername("joe");
        webUser.setPassword("mama");
        webUser.setFullName(new FullName("Joe", "Mama"));
        return webUser;
    }
}
