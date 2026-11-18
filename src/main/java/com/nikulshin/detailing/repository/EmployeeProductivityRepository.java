package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.EmployeeProductivityRecord;
import com.nikulshin.detailing.model.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeProductivityRepository extends JpaRepository<EmployeeProductivityRecord, Long> {

    // Записи продуктивности сотрудника за конкретный месяц
    @Query("SELECT r FROM EmployeeProductivityRecord r " +
            "WHERE r.employee = :employee " +
            "AND YEAR(r.date) = :year AND MONTH(r.date) = :month " +
            "ORDER BY r.date ASC, r.id ASC")
    List<EmployeeProductivityRecord> findByEmployeeAndYearAndMonth(@Param("employee") User employee,
                                                                   @Param("year") int year,
                                                                   @Param("month") int month);
}
