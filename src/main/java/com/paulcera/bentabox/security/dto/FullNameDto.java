package com.paulcera.bentabox.security.dto;


import com.paulcera.bentabox.security.model.FullName;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FullNameDto {

    @NotBlank(message = "First Name is required")
    private String firstName;

    @NotBlank(message = "Last Name is required")
    private String lastName;

    public FullNameDto(FullName fullName) {
        this.firstName = fullName.getFirstName();
        this.lastName = fullName.getLastName();
    }
}
