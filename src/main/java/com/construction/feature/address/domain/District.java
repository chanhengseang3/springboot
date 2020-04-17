package com.construction.feature.address.domain;

import com.construction.persistence.domain.VersionEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class District extends VersionEntity {

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;
}
