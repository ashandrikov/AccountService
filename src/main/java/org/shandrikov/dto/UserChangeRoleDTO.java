package org.shandrikov.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserChangeRoleDTO {
    @JsonProperty("user")
    private String email;
    private String role;
    private String operation;

    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }

    public void setRole(String role) {
        this.role = role.toUpperCase();
    }

    public void setOperation(String operation) {
        this.operation = operation.toUpperCase();
    }

    public String getRole() {
        return role;
    }
}
