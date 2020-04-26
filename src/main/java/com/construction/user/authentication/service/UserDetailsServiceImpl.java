package com.construction.user.authentication.service;

import com.construction.user.authorization.domain.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AppUserService service;

    @Override
    public UserDetails loadUserByUsername(final String name) throws UsernameNotFoundException {
        final var appUser = service.getUserByUserName(name);
        final var authorities = appUser.getRole().getPermissions().stream().map(this::getAuthority).collect(Collectors.toList());
        return new User(appUser.getUserName(), appUser.getPassword(), authorities);
    }

    private GrantedAuthority getAuthority(Permission permission) {
        return new SimpleGrantedAuthority(permission.getCodeName());
    }
}
