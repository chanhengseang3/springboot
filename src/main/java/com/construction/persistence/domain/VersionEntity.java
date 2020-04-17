package com.construction.persistence.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.io.Serializable;

@MappedSuperclass
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class VersionEntity implements Serializable, Persistable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    @JsonIgnore
    @GraphQLIgnore
    private Integer version;

    @Override
    @JsonIgnore
    @GraphQLIgnore
    public boolean isNew() {
        return version == null;
    }
}
