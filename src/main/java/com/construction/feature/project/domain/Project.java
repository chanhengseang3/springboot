package com.construction.feature.project.domain;

import com.construction.feature.status.ObjectStatus;
import com.construction.persistence.domain.AuditingEntity;
import com.construction.user.authentication.domain.AppUser;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class Project extends AuditingEntity {

    @Column(nullable = false)
    private String objectType;

    @Column(nullable = false)
    private String objectName;

    private String code;

    private ObjectStatus status;

    @ManyToOne
    @JoinColumn
    private AppUser approvedBy;

    private LocalDateTime approveAt;

    @Column(columnDefinition = "mediumtext")
    private String description;
}
