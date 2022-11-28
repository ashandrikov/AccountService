package org.shandrikov.configuration;

import org.shandrikov.entity.User;
import org.shandrikov.enums.EventAction;
import org.shandrikov.service.impl.SecurityEventsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
    public static final String ACCESS_DENIED_USER = "Access for user %s denied";
    @Autowired
    SecurityEventsServiceImpl securityEventsService;
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        UserDetails auth = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        securityEventsService.registerEvent(EventAction.ACCESS_DENIED, auth.getUsername(), request.getServletPath(), request.getServletPath());
        LOGGER.info(ACCESS_DENIED_USER, auth.getUsername());
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied!");
    }
}
