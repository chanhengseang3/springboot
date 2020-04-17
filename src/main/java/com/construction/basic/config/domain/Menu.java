package com.construction.basic.config.domain;

import com.construction.persistence.domain.ExtendedEntity;
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
public class Menu extends ExtendedEntity {

    @Column(nullable = false)
    private String name;

    private String path;

    private int priority;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Menu parent;
}
