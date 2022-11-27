package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOutDTO {
    private Long id;
    private String name;
    private String lastname;
    private String email;
    private List<String> roles;
    private boolean accountNonLocked;
    private int failedAttempt;
}
