package com.construction.basic.config.domain;

import com.construction.persistence.domain.ExtendedEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class PageConfiguration extends ExtendedEntity {

    private String name;

    private String shortDescription;

    private String slideImage;
}
