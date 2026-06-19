package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.ActRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActRecordRepository extends JpaRepository<ActRecord, Long> {

    // Выборка актов за конкретный месяц и год
    @Query("SELECT a FROM ActRecord a WHERE YEAR(a.date) = :year AND MONTH(a.date) = :month ORDER BY a.date DESC")
    List<ActRecord> findByYearAndMonth(@Param("year") int year, @Param("month") int month);
}