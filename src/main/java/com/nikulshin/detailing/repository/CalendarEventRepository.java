package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    // Выборка событий, попадающих в диапазон дат (включительно), используется для отображения в календаре
    @Query("SELECT e FROM CalendarEvent e WHERE e.date BETWEEN :start AND :end ORDER BY e.date ASC")
    List<CalendarEvent> findByDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
