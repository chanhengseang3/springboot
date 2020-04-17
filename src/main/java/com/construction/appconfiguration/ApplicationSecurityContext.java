package com.construction.appconfiguration;

import com.construction.user.authentication.domain.AppUser;
import com.construction.user.authentication.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ApplicationSecurityContext {

    @Autowired
    private AppUserService service;

    public Object getAuthPrinciple(){
        final SecurityContext context = SecurityContextHolder.getContext();
        final Authentication auth = context == null ? null : context.getAuthentication();
        return auth == null ? null : auth.getPrincipal();
    }

    public AppUser authenticatedUser() {
        final var principal = getAuthPrinciple();
        if (principal instanceof AppUser) {
            return (AppUser) principal;
        } else if (principal instanceof String && !((String) principal).equalsIgnoreCase("anonymousUser")) {
            return service.getUserByUserName((String) principal);
        }
        return null;
    }
}
