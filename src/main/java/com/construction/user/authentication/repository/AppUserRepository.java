package com.construction.user.authentication.repository;

import com.construction.user.authentication.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUserName(final String name);

    Optional<AppUser> findByEmail(final String email);

    Optional<AppUser> findByMobile(final String mobile);
}
