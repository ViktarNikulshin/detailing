package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.FinanceType;
import com.nikulshin.detailing.model.domain.PaymentCalendarBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentCalendarBalanceRepository extends JpaRepository<PaymentCalendarBalance, Long> {

    Optional<PaymentCalendarBalance> findByYearAndMonthAndAccountId(Integer year, Integer month, FinanceType accountId);

    List<PaymentCalendarBalance> findByYearAndMonth(Integer year, Integer month);

    Optional<PaymentCalendarBalance> findFirstByAccountIdOrderByYearAscMonthAsc(FinanceType accountId);
}
