package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.ActRecord;
import com.nikulshin.detailing.model.domain.DebtRecord;
import com.nikulshin.detailing.model.domain.DebtType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebtRecordRepository extends JpaRepository<DebtRecord, Long> {

    // Выборка актов за конкретный месяц и год
    @Query("SELECT a FROM DebtRecord a WHERE YEAR(a.date) = :year AND MONTH(a.date) = :month ORDER BY a.date DESC")
    List<DebtRecord> findByYearAndMonth(@Param("year") int year, @Param("month") int month);

    // Выборка долгов за конкретный месяц, год и тип
    @Query("SELECT a FROM DebtRecord a WHERE YEAR(a.date) = :year AND MONTH(a.date) = :month AND a.debtType = :type ORDER BY a.date DESC")
    List<DebtRecord> findByYearAndMonthAndType(@Param("year") int year, @Param("month") int month, @Param("type") DebtType type);
}