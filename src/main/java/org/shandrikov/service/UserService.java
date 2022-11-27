package org.shandrikov.service;

import org.shandrikov.dto.LockUserInDTO;
import org.shandrikov.dto.PasswordDTO;
import org.shandrikov.dto.UserChangeRoleInDTO;
import org.shandrikov.dto.UserInDTO;
import org.shandrikov.dto.UserOutDTO;
import org.shandrikov.entity.User;
import org.shandrikov.enums.EventAction;
import org.shandrikov.enums.OperationLocks;
import org.shandrikov.enums.OperationRoles;
import org.shandrikov.enums.Role;
import org.shandrikov.exception.EmployeeNotFoundException;
import org.shandrikov.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import org.shandrikov.repository.UserRepository;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.shandrikov.enums.EventAction.BRUTE_FORCE;
import static org.shandrikov.enums.EventAction.GRANT_ROLE;
import static org.shandrikov.enums.EventAction.LOCK_USER;
import static org.shandrikov.enums.EventAction.REMOVE_ROLE;
import static org.shandrikov.enums.EventAction.UNLOCK_USER;
import static org.shandrikov.enums.OperationLocks.LOCK;
import static org.shandrikov.enums.OperationRoles.GRANT;
import static org.shandrikov.enums.Role.ADMINISTRATOR;
import static org.shandrikov.enums.Role.USER;
import static org.shandrikov.enums.Role.getAllRolesStrings;
import static org.shandrikov.util.StringPool.CANNOT_LOCK_ADMINISTRATOR;
import static org.shandrikov.util.StringPool.CANNOT_REMOVE_ADMINISTRATOR;
import static org.shandrikov.util.StringPool.LAST_ROLE;
import static org.shandrikov.util.StringPool.NOT_COMBINE_ADMINISTRATIVE_AND_BUSINESS_ROLES;
import static org.shandrikov.util.StringPool.PASSWORDS_EQUAL;
import static org.shandrikov.util.StringPool.PASSWORD_HACKED;
import static org.shandrikov.util.StringPool.PASSWORD_UPDATED_USER;
import static org.shandrikov.util.StringPool.ROLE_NOT_FOUND;
import static org.shandrikov.util.StringPool.USER_CREATED;
import static org.shandrikov.util.StringPool.USER_DOES_NOT_HAVE_ROLE;
import static org.shandrikov.util.StringPool.USER_EXISTS;
import static org.shandrikov.util.StringPool.USER_LOCKED_BRUTE;
import static org.shandrikov.util.StringPool.USER_NOT_FOUND;

@Service
@Validated
public class UserService implements UserDetailsService {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    UserRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    SecurityEventsService securityEventsService;

    Set<String> hackedPasswords = Set.of("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
            "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Not found: " + username));
    }

    public UserOutDTO saveUser(@Valid UserInDTO userDTO) {
        User user = UserMapper.convertDtoToEntity(userDTO);
        if (hackedPasswords.contains(user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, PASSWORD_HACKED);
        }
        user.setPassword(encoder.encode(user.getPassword()));
        if (findAll().isEmpty()) {
            user.grantRole(ADMINISTRATOR);
        } else {
            user.grantRole(USER);
        }
        User userToSave = User.builder()
                .name(user.getName())
                .lastname(user.getLastname())
                .email(user.getEmail().toLowerCase())
                .password(user.getPassword())
                .userGroups((Set<Role>) user.getAuthorities())
                .accountNonLocked(true)
                .build();
        try {
            userRepository.save(userToSave);
            securityEventsService.registerEvent(EventAction.CREATE_USER, "Anonymous", userToSave.getEmail(), "/api/auth/signup");
            LOGGER.info(String.format(USER_CREATED, userToSave.getEmail()));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_EXISTS);
        }
        User userByEmail = userRepository.findByEmail(user.getEmail()).get();
        return UserMapper.convertEntityToDTO(userByEmail);
    }

    public void updatePassword(UserDetails userDetails, @Valid PasswordDTO passwordDTO) {
        User userByEmail = userRepository.findByEmail(userDetails.getUsername()).get();
        String newPassword = passwordDTO.getNewPassword();
        if (hackedPasswords.contains(newPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, PASSWORD_HACKED);
        } else if (encoder.matches(newPassword, userByEmail.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, PASSWORDS_EQUAL);
        } else {
            userByEmail.setPassword(encoder.encode(newPassword));
            userRepository.save(userByEmail);
            securityEventsService.registerEvent(EventAction.CHANGE_PASSWORD, userDetails.getUsername(), userDetails.getUsername(), "/api/admin/user");
            LOGGER.info(String.format(PASSWORD_UPDATED_USER, userDetails.getUsername()));
        }
    }

    public List<UserOutDTO> findAll() {
        List<User> userList = (List<User>) userRepository.findAll();
        return userList.stream()
                .map(UserMapper::convertEntityToDTO)
                .toList();
    }

    public void deleteUser(String email) {
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND);
        } else if (userByEmail.get().getAuthorities().contains(ADMINISTRATOR)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, CANNOT_REMOVE_ADMINISTRATOR);
        }

        UserDetails auth = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        securityEventsService.registerEvent(EventAction.DELETE_USER, auth.getUsername(), email, "/api/admin/user");
        LOGGER.info(String.format("User %s was deleted", email));
        userRepository.delete(userByEmail.get());
    }

    public UserOutDTO changeUserRole(UserChangeRoleInDTO userDTO) {
        User userByEmail = userRepository.findByEmail(userDTO.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
        if (!getAllRolesStrings().contains(userDTO.getRole())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ROLE_NOT_FOUND);
        } else if (userDTO.getOperation().equals(OperationRoles.REVOKE.toString()) && !userByEmail.getAuthorities().contains(Role.valueOf(userDTO.getRole()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_DOES_NOT_HAVE_ROLE);
        } else if (userDTO.getOperation().equals(OperationRoles.REVOKE.toString()) && userDTO.getRole().equals(ADMINISTRATOR.toString())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, CANNOT_REMOVE_ADMINISTRATOR);
        } else if (userDTO.getOperation().equals(OperationRoles.REVOKE.toString()) && userByEmail.getAuthorities().size() == 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, LAST_ROLE);
        } else if (userDTO.getOperation().equals(GRANT.toString()) && userByEmail.getAuthorities().contains(ADMINISTRATOR)
                || !userByEmail.getAuthorities().contains(ADMINISTRATOR) && userDTO.getRole().equals(ADMINISTRATOR.toString())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, NOT_COMBINE_ADMINISTRATIVE_AND_BUSINESS_ROLES);
        }

        String object;
        UserDetails auth = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        switch (OperationRoles.valueOf(userDTO.getOperation())){
            case GRANT:
                userByEmail.grantRole(Role.valueOf(userDTO.getRole()));
                object = String.format("Grant role %s to %s", userDTO.getRole(), userDTO.getEmail());
                securityEventsService.registerEvent(GRANT_ROLE, auth.getUsername(), object, "/api/admin/user/role");
                LOGGER.info(String.format("Role %s was GRANTED for user %s", userDTO.getRole(), userDTO.getEmail()));
                break;
            case REVOKE:
                userByEmail.removeRole(Role.valueOf(userDTO.getRole()));
                object = String.format("Revoke role %s from %s", userDTO.getRole(), userDTO.getEmail());
                securityEventsService.registerEvent(REMOVE_ROLE, auth.getUsername(), object, "/api/admin/user/role");
                LOGGER.info(String.format("Role %s was REVOKED from user %s", userDTO.getRole(), userDTO.getEmail()));
                break;
            default: return null;
        }
        return UserMapper.convertEntityToDTO(userRepository.save(userByEmail));
    }

    public Map<String, String> lockOrUnlockUser(LockUserInDTO userDTO) {
        User userByEmail = userRepository.findByEmailIgnoreCase(userDTO.getEmail())
                .orElseThrow(EmployeeNotFoundException::new);
        if (userByEmail.getAuthorities().contains(ADMINISTRATOR)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, CANNOT_LOCK_ADMINISTRATOR);
        }

        String object;
        UserDetails auth = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (userDTO.getOperation().equals(LOCK.toString()) && userByEmail.isAccountNonLocked()) {
            userByEmail.setAccountNonLocked(false);
            userRepository.save(userByEmail);
            object = String.format("Lock user %s", userByEmail.getEmail());
            securityEventsService.registerEvent(LOCK_USER, auth.getUsername(), object, "/api/admin/user/access");
            LOGGER.info(String.format("User %s was LOCKED", userByEmail.getEmail()));
        } else if (userDTO.getOperation().equals(OperationLocks.UNLOCK.toString()) && !userByEmail.isAccountNonLocked()) {
            userByEmail.setAccountNonLocked(true);
            userByEmail.setFailedAttempt(0);
            userRepository.save(userByEmail);
            object = String.format("Unlock user %s", userByEmail.getEmail());
            securityEventsService.registerEvent(UNLOCK_USER, auth.getUsername(), object, "/api/admin/user/access");
            LOGGER.info(String.format("User %s was UNLOCKED", userByEmail.getEmail()));
        } else {
            return Map.of("status", "Smth went wrong!");
        }
        return Map.of("status", String.format("User %s %sed!", userDTO.getEmail().toLowerCase(), userDTO.getOperation().toLowerCase()));
    }

    public void increaseFailedAttempts(User user, String path) {
        user.setFailedAttempt(user.getFailedAttempt() + 1);
        if (user.getFailedAttempt() > MAX_FAILED_ATTEMPTS)
            lockUser(user, path);
        userRepository.save(user);
    }

    public void lockUser(User user, String path) {
        user.setAccountNonLocked(false);
        securityEventsService.registerEvent(BRUTE_FORCE, user.getEmail(), path, path);
        securityEventsService.registerEvent(LOCK_USER, user.getEmail(), String.format("Lock user %s", user.getEmail()), path);
        LOGGER.info(String.format(USER_LOCKED_BRUTE, user.getEmail()));
    }
}
