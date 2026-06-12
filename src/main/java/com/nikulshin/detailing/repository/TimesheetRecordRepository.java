package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.TimesheetRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimesheetRecordRepository extends JpaRepository<TimesheetRecord, Long> {

    // Поиск всех записей мастера за конкретный промежуток дат
    List<TimesheetRecord> findByMasterIdAndRecordDateBetween(Long masterId, LocalDate start, LocalDate end);
}