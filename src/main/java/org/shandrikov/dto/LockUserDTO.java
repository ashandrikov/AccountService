package org.shandrikov.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LockUserDTO {
    @JsonProperty("user")
    private String email;
    private String operation;
}
