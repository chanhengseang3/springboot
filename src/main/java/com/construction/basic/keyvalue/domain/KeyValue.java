package com.construction.basic.keyvalue.domain;

import com.construction.persistence.utils.JsonObjectConverter;
import com.construction.persistence.domain.AuditingEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Entity
@Getter
@Setter
@Table(name = "key_value")
@Accessors(chain = true)
@SQLDelete(sql = "delete from key_value where removable <> 0 and id = ?", check = ResultCheckStyle.COUNT)
public class KeyValue extends AuditingEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "key_value_group")
    private KeyValueGroup group = KeyValueGroup.OTHER;

    @NotNull
    @Column(name = "key_", nullable = false)
    private String key;

    private String value;

    @Column(columnDefinition = "text")
    @Convert(converter = JsonObjectConverter.class)
    private Map<String, Object> jsonValue;

    private String remarks;

    @Column(updatable = false)
    private boolean removable = true;
}
