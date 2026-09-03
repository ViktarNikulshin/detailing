package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    @Query("SELECT e FROM CalendarEvent e WHERE e.date BETWEEN :start AND :end ORDER BY e.date ASC")
    List<CalendarEvent> findByDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    Optional<CalendarEvent> findByGoogleEventId(String googleEventId);

    List<CalendarEvent> findBySyncStatus(CalendarEvent.SyncStatus syncStatus);

    List<CalendarEvent> findByCreatedOrderId(Long orderId);
}
