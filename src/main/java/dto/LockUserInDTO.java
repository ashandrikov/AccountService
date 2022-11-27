package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LockUserInDTO {
    @JsonProperty("user")
    private String email;
    private String operation;
}
