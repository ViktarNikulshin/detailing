package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.MasterSalaryLog;
import com.nikulshin.detailing.model.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MasterSalaryRepository extends JpaRepository<MasterSalaryLog, Long> {

    List<MasterSalaryLog> findAllByMasterIsAndDateAfterAndDateBefore(User master, LocalDateTime start, LocalDateTime end);
}
