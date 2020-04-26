package com.construction.user.authorization.domain;

import com.construction.persistence.domain.VersionEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class UserRole extends VersionEntity {

    @Column(nullable = false)
    private String name;

    @OneToMany
    @JoinTable
    private List<Permission> permissions;
}
