package com.paulcera.bentabox.security.dto;

public class WebUserFormMother {

    public static WebUserForm joe() {
        WebUserForm form = new WebUserForm();
        form.setUsername("joe");
        form.setPassword("joemama");
        form.setFullName(new FullNameDto("Joe", "Mama"));
        return form;
    }

}
