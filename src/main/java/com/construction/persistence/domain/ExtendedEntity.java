package com.construction.persistence.domain;

import com.construction.persistence.utils.JsonObjectConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.MappedSuperclass;
import java.util.Map;

@MappedSuperclass
@Getter
@Setter
public abstract class ExtendedEntity extends VersionEntity {

    @Column(columnDefinition = "text")
    @Convert(converter = JsonObjectConverter.class)
    private Map<String, Object> info;
}
