package com.construction.feature.address.domain;

import com.construction.persistence.domain.VersionEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class Province extends VersionEntity {

    private String name;
}
