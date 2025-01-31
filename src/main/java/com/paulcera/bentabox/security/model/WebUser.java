package com.paulcera.bentabox.security.model;

import com.paulcera.bentabox.security.dto.WebUserForm;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WebUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Embedded
    private FullName fullName;

    public static WebUser createFromForm(WebUserForm form, PasswordEncoder passwordEncoder) {
        WebUser webUser = new WebUser();
        webUser.setUsername(form.getUsername());
        webUser.setPassword(passwordEncoder.encode(form.getPassword()));
        webUser.setFullName(new FullName(form.getFullName()));
        return webUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WebUser webUser = (WebUser) o;
        return id.equals(webUser.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
