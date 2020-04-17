package com.construction.feature.address.domain;

import com.construction.user.authentication.domain.AppUser;
import com.construction.persistence.domain.VersionEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class Address extends VersionEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne
    @JoinColumn(name = "district_id")
    private District district;

    private String details;

    @Embedded
    private Location location;
}
