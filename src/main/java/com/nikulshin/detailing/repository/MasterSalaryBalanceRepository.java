package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.MasterSalaryBalance;
import com.nikulshin.detailing.model.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterSalaryBalanceRepository extends JpaRepository<MasterSalaryBalance, Long> {

    Optional<MasterSalaryBalance> findByMasterAndYearAndMonth(User master, Integer year, Integer month);
}
