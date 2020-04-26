package com.construction.user.authentication.domain;

import com.construction.persistence.domain.VersionEntity;
import com.construction.user.authorization.domain.UserRole;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.Email;

@Entity
@Getter
@Setter
@Table(name = "user", uniqueConstraints = @UniqueConstraint(columnNames = "user_name"))
@Accessors(chain = true)
@Where(clause = "status <> 'DELETED'")
@SQLDelete(sql = "update user set status = 'DELETED' where id = ?")
public class AppUser extends VersionEntity {

    @Column(name = "user_name", unique = true, nullable = false)
    private String userName;

    @Email
    private String email;

    private String mobile;

    @Column(nullable = false)
    private String password;

    @OneToOne
    @JoinColumn
    private UserRole role;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;
}
