package com.construction.user.authentication.service;

import com.construction.user.authentication.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AppUserService service;

    @Override
    public UserDetails loadUserByUsername(final String name) throws UsernameNotFoundException {
        final AppUser appUser = service.getUserByUserName(name);
        GrantedAuthority authority = new SimpleGrantedAuthority(appUser.getRole().name());
        return new User(appUser.getUserName(), appUser.getPassword(), Collections.singletonList(authority));
    }
}
