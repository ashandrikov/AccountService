package org.shandrikov.mapper;

import org.shandrikov.dto.UserInDTO;
import org.shandrikov.dto.UserOutDTO;
import org.shandrikov.entity.User;

public class UserMapper {
    public static User convertDtoToEntity(UserInDTO userInDTO) {
        return User.builder()
                .name(userInDTO.getName())
                .lastname(userInDTO.getLastname())
                .email(userInDTO.getEmail().toLowerCase())
                .password(userInDTO.getPassword())
                .build();
    }

    public static UserOutDTO convertEntityToDTO(User userEntity) {
        return UserOutDTO.builder()
                .id(userEntity.getId())
                .name(userEntity.getName())
                .lastname(userEntity.getLastname())
                .email(userEntity.getEmail())
                .roles(userEntity.getAuthorities().stream()
                        .map(el -> "ROLE_" + el)
                        .sorted(String::compareTo)
                        .toList())
                .failedAttempt(userEntity.getFailedAttempt())
                .accountNonLocked(userEntity.isAccountNonLocked())
                .build();
    }
}
