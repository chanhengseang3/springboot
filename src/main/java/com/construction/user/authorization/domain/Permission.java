package com.construction.user.authorization.domain;

import com.construction.persistence.domain.VersionEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class Permission extends VersionEntity {

    private String actionName;

    private String entityName;

    private String codeName;
}
