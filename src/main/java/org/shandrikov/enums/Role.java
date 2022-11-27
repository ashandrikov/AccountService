package org.shandrikov.enums;

import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

public enum Role implements GrantedAuthority {
    ADMINISTRATOR, USER, ACCOUNTANT, AUDITOR;
    @Override
    public String getAuthority() {
        return name();
    }
    public static Set<String> getAllRolesStrings(){
        return Set.of(USER.toString(), ACCOUNTANT.toString(), ADMINISTRATOR.toString(), AUDITOR.toString());
    }
}
