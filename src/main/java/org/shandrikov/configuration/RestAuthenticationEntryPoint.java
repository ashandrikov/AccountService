package org.shandrikov.configuration;

import org.shandrikov.entity.User;
import org.shandrikov.enums.EventAction;
import org.shandrikov.enums.Role;
import org.shandrikov.repository.UserRepository;
import org.shandrikov.service.SecurityEventsService;
import org.shandrikov.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);
    public static final String LOGIN_FAILED_USER = "Login for user %s failed";
    @Autowired
    SecurityEventsService securityEventsService;
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            String username = new String(Base64.getDecoder().decode(authorization.split("\\s+")[1])).split(":")[0];
            String path = ServletUriComponentsBuilder.fromCurrentRequest().build().getPath();

            if (userRepository.findByEmailIgnoreCase(username).isEmpty()){
                securityEventsService.registerEvent(EventAction.LOGIN_FAILED, username, path, path);
                LOGGER.info(String.format(LOGIN_FAILED_USER, username));
            } else {
                User user = userRepository.findByEmailIgnoreCase(username).get();
                if (user.isAccountNonLocked()) {
                    securityEventsService.registerEvent(EventAction.LOGIN_FAILED, user.getEmail(), path, path);
                    LOGGER.info(String.format(LOGIN_FAILED_USER, username));
                    if (!user.getAuthorities().contains(Role.ADMINISTRATOR))
                        userService.increaseFailedAttempts(user, path);
                }
            }
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}
