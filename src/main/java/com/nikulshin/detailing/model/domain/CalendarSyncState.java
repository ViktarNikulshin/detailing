package com.nikulshin.detailing.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Single-row state holder for the Google Calendar incremental sync.
 * Stores the {@code nextSyncToken} returned by the Calendar API so that
 * subsequent syncs fetch only changed events instead of the full window.
 */
@Entity
@Table(name = "calendar_sync_state")
@Data
public class CalendarSyncState {

    public static final Long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(name = "next_sync_token", length = 512)
    private String nextSyncToken;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
