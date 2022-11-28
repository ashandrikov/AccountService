package org.shandrikov.controller;

import org.shandrikov.dto.LockUserDTO;
import org.shandrikov.dto.PasswordDTO;
import org.shandrikov.dto.UserChangeRoleDTO;
import org.shandrikov.dto.UserInDTO;
import org.shandrikov.dto.UserOutDTO;
import org.shandrikov.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.shandrikov.service.impl.UserServiceImpl;

import java.util.List;
import java.util.Map;

import static org.shandrikov.util.StringPool.PASSWORD_UPDATED;


@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    UserServiceImpl userService;

    @PostMapping("/auth/signup")
    public UserOutDTO signup(@RequestBody UserInDTO user){
        return userService.saveUser(user);
    }

    @PostMapping("/auth/changepass")
    public Map<String, String> changePassword(@RequestBody PasswordDTO passwordDTO, @AuthenticationPrincipal User user){
        userService.updatePassword(user, passwordDTO);
        return Map.of("email", user.getUsername(), "status", PASSWORD_UPDATED);
    }

    @GetMapping("/admin/user")
    public List<UserOutDTO> seeListUsers(){
        return userService.findAll();
    }

    @DeleteMapping("/admin/user/{email}")
    public Map<String, String> deleteUser(@PathVariable("email") String email){
        userService.deleteUser(email);
        return Map.of("user", email, "status", "Deleted successfully!");
    }

    @PutMapping("/admin/user/role")
    public UserOutDTO changeUserRoles(@RequestBody UserChangeRoleDTO userDTO){
        return userService.changeUserRole(userDTO);
    }

    @PutMapping("/admin/user/access")
    public Map<String, String> lockUnlockUser(@RequestBody LockUserDTO userDTO){
        return userService.lockOrUnlockUser(userDTO);
    }
}
