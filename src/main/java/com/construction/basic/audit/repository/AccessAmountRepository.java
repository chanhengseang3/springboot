package com.construction.basic.audit.repository;

import com.construction.basic.audit.domain.AccessAmount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Month;
import java.util.Optional;

public interface AccessAmountRepository extends JpaRepository<AccessAmount, Long> {
    Optional<AccessAmount> findByYearAndMonth(final int year, final Month month);
}
