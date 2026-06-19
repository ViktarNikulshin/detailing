package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.FinanceType;
import com.nikulshin.detailing.model.domain.FinanceBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FinanceBalanceRepository extends JpaRepository<FinanceBalance, Long> {
    Optional<FinanceBalance> findByYearAndMonthAndFinanceType(Integer year, Integer month, FinanceType type);
}
