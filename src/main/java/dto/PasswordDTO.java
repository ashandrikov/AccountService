package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class PasswordDTO {
    @Length(min = 12, message = "Password length must be 12 chars minimum!")
    @JsonProperty("new_password")
    private String newPassword;
}
