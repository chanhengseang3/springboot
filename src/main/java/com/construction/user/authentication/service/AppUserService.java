package com.construction.user.authentication.service;

import com.construction.appconfiguration.ApplicationSecurityContext;
import com.construction.exception.PasswordInvalidException;
import com.construction.persistence.exception.ResourceNotFoundException;
import com.construction.user.authentication.domain.AppUser;
import com.construction.user.authentication.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Service
@Transactional
public class AppUserService {

    @Autowired
    private AppUserRepository repository;

    @Autowired
    private ApplicationSecurityContext context;

    @Autowired
    private PasswordEncoder encoder;

    public AppUser getUserByUserName(final String name) {
        return repository.findByUserName(name).orElseThrow(() -> new ResourceNotFoundException(AppUser.class, name));
    }

    public AppUser getUserByEmail(@Email @NotNull final String email) {
        return repository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException(AppUser.class, email));
    }

    public AppUser changePassword(final String oldPass, final String newPass) {
        final var user = context.authenticatedUser();
        if (user != null && encoder.matches(oldPass, user.getPassword())) {
            final var appUser = repository.findById(user.getId()).orElseThrow();
            appUser.setPassword(encoder.encode(newPass));
            return repository.save(appUser);
        }
        throw new PasswordInvalidException();
    }

    public AppUser updateUser(final AppUser appUser) {
        appUser.setPassword(encoder.encode(appUser.getPassword()));
        return repository.save(appUser);
    }

    public AppUser createUser(final AppUser appUser) {
        appUser.setPassword(encoder.encode(appUser.getPassword()));
        return repository.save(appUser);
    }
}
