package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.CalendarSyncState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalendarSyncStateRepository extends JpaRepository<CalendarSyncState, Long> {
}
