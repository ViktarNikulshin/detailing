package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.FinanceType;
import com.nikulshin.detailing.model.domain.FinanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface FinanceRecordRepository extends JpaRepository<FinanceRecord, Long> {

    // Выбираем записи за конкретный месяц
    @Query("SELECT f FROM FinanceRecord f WHERE f.recordDate BETWEEN :start AND :end AND f.financeType = :type ORDER BY f.recordDate ASC")
    List<FinanceRecord> findByMonthRange(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("type")FinanceType type);
}