package org.shandrikov.service;

import org.shandrikov.dto.LockUserDTO;
import org.shandrikov.dto.PasswordDTO;
import org.shandrikov.dto.UserChangeRoleDTO;
import org.shandrikov.dto.UserInDTO;
import org.shandrikov.dto.UserOutDTO;
import org.shandrikov.entity.User;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

public interface UserService {
    UserOutDTO saveUser(@Valid UserInDTO userDTO);
    void updatePassword(User user, @Valid PasswordDTO passwordDTO);
    List<UserOutDTO> findAll();
    void deleteUser(String email);
    UserOutDTO changeUserRole(UserChangeRoleDTO userDTO);
    Map<String, String> lockOrUnlockUser(LockUserDTO userDTO);
    void increaseFailedAttempts(User user, String path);
}
