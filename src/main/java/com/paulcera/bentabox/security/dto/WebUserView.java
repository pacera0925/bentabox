package com.paulcera.bentabox.security.dto;


import com.paulcera.bentabox.security.model.WebUser;
import lombok.Data;

@Data
public class WebUserView {

    private Integer id;

    private String username;

    private FullNameDto fullName;


    public WebUserView(WebUser newWebUser) {
        this.id = newWebUser.getId();
        this.username = newWebUser.getUsername();
        this.fullName = new FullNameDto(newWebUser.getFullName());
    }
}
