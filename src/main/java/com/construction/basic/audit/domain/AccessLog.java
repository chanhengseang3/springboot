package com.construction.basic.audit.domain;

import com.construction.persistence.domain.VersionEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class AccessLog extends VersionEntity {

    private String remoteAddress;

    private LocalDateTime accessAt;
}
