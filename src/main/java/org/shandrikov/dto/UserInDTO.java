package org.shandrikov.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class UserInDTO {
    @NotBlank(message = "Name must not be empty")
    private String name;

    @NotBlank(message = "Lastname must not be empty")
    private String lastname;

    @NotBlank(message = "Email must not be empty")
    @Pattern(message = "Email is not valid (regexp)", regexp = ".+@acme\\.com")
    private String email;

    @Length(min = 12, message = "Password length must be 12 chars minimum!")
    @NotBlank(message = "Password must not be empty")
    private String password;
}
