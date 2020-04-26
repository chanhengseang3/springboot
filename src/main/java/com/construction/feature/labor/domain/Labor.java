package com.construction.feature.labor.domain;

import com.construction.feature.house.domain.House;
import com.construction.persistence.domain.AuditingEntity;
import com.construction.user.authentication.domain.AppUser;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class Labor extends AuditingEntity {

    private String typeOfWork;

    @JoinColumn
    @ManyToOne
    private House house;

    private String code;

    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Labor parent;

    private boolean leaf;

    private String floor;

    private String boq;

    private String contractType;

    private Integer quantity;

    private String unit;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    private BigDecimal actualPrice;

    @ManyToOne
    @JoinColumn
    private AppUser approvedBy;

    private LocalDateTime approvedAt;
}
