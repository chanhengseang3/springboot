package com.construction.feature.house.domain;

import com.construction.feature.status.ObjectStatus;
import com.construction.feature.project.domain.Project;
import com.construction.persistence.domain.AuditingEntity;
import com.construction.user.authentication.domain.AppUser;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class House extends AuditingEntity {

    @ManyToOne
    @JoinColumn
    private Project project;

    private String typeOfHouse;

    private String street;

    private String houseNo;

    @Enumerated(EnumType.STRING)
    private ObjectStatus status;

    private Float houseWidth;

    private Float houseLong;

    private Float landWidth;

    private Float landLong;

    @ManyToOne
    @JoinColumn
    private AppUser approvedBy;

    private LocalDateTime approvedAt;
}
