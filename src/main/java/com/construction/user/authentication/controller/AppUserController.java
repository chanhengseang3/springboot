package com.construction.user.authentication.controller;

import com.construction.user.authentication.repository.AppUserRepository;
import com.construction.user.authentication.service.AppUserService;
import com.construction.persistence.exception.ResourceNotFoundException;
import com.construction.persistence.service.EntityDataMapper;
import com.construction.user.authentication.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/user")
public class AppUserController {

    @Autowired
    private AppUserRepository repository;

    @Autowired
    private AppUserService service;

    @Autowired
    private EntityDataMapper entityDataMapper;

    @GetMapping
    public List<AppUser> getAllUser() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public AppUser getUserById(@NotNull @PathVariable("id") final Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(AppUser.class, id));
    }

    @PutMapping("/changePassword")
    public AppUser changePassword(@RequestParam("old") final String oldPassword,
                                  @RequestParam("new") final String newPassword) {
        return service.changePassword(oldPassword, newPassword);
    }

    @PutMapping("/{id}")
    public AppUser updateUser(@NotNull @PathVariable final Long id, @RequestBody final AppUser sourceUser) {
        final var targetUser = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(AppUser.class, id));
        try {
            final var user = entityDataMapper.mapObject(sourceUser, targetUser, AppUser.class);
            return service.updateUser(user);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @PostMapping
    public AppUser createUser(@NotNull @RequestBody final AppUser appUser) {
        return service.createUser(appUser);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@NotNull @PathVariable("id") final Long id) {
        repository.deleteById(id);
    }
}
