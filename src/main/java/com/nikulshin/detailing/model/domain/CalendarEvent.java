package com.nikulshin.detailing.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "calendar_events", indexes = {
        @Index(name = "idx_calendar_event_google_id", columnList = "google_event_id", unique = true),
        @Index(name = "idx_calendar_event_sync_status", columnList = "sync_status"),
        @Index(name = "idx_calendar_event_date", columnList = "event_date")
})
@Data
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_date", nullable = false)
    private LocalDate date;

    @Column(name = "event_end_date")
    private LocalDate endDate;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "google_event_id", unique = true, length = 255)
    private String googleEventId;

    @Column(name = "google_calendar_id", length = 255)
    private String googleCalendarId;

    @Column(name = "sync_status", nullable = false, columnDefinition = "varchar(255) default 'NEW'")
    @Enumerated(EnumType.STRING)
    private SyncStatus syncStatus = SyncStatus.NEW;

    @Column(name = "parsed_name", length = 255)
    private String parsedName;

    @Column(name = "parsed_phone", length = 50)
    private String parsedPhone;

    @Column(name = "parsed_car_brand", length = 255)
    private String parsedCarBrand;

    @Column(name = "parsed_car_model", length = 255)
    private String parsedCarModel;

    @Column(name = "parsed_comment", length = 1000)
    private String parsedComment;

    @Column(name = "sync_error", length = 1000)
    private String syncError;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "created_order_id")
    private Long createdOrderId;

    public enum SyncStatus {
        NEW,
        PARSED,
        SYNCED,
        FAILED,
        MANUAL_REVIEW
    }
}
