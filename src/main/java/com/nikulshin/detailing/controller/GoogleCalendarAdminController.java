package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.domain.CalendarEvent;
import com.nikulshin.detailing.repository.CalendarEventRepository;
import com.nikulshin.detailing.scheduler.GoogleCalendarSyncScheduler;
import com.nikulshin.detailing.service.google.GoogleCalendarSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/google-calendar")
@RequiredArgsConstructor
public class GoogleCalendarAdminController {

    private final GoogleCalendarSyncService syncService;
    private final CalendarEventRepository calendarEventRepository;
    private final GoogleCalendarSyncScheduler syncScheduler;

    @PostMapping("/sync")
    public ResponseEntity<GoogleCalendarSyncService.SyncResult> manualSync(
            @RequestParam(required = false) Integer lookbackDays,
            @RequestParam(required = false) Integer lookaheadDays
    ) {
        LocalDateTime timeMin = LocalDateTime.now().minusDays(lookbackDays != null ? lookbackDays : 30);
        LocalDateTime timeMax = LocalDateTime.now().plusDays(lookaheadDays != null ? lookaheadDays : 60);
        return ResponseEntity.ok(syncService.syncEvents(timeMin, timeMax));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getSyncStats() {
        long total = calendarEventRepository.count();
        long newCount = calendarEventRepository.findBySyncStatus(CalendarEvent.SyncStatus.NEW).size();
        long parsed = calendarEventRepository.findBySyncStatus(CalendarEvent.SyncStatus.PARSED).size();
        long synced = calendarEventRepository.findBySyncStatus(CalendarEvent.SyncStatus.SYNCED).size();
        long failed = calendarEventRepository.findBySyncStatus(CalendarEvent.SyncStatus.FAILED).size();
        long review = calendarEventRepository.findBySyncStatus(CalendarEvent.SyncStatus.MANUAL_REVIEW).size();

        return ResponseEntity.ok(Map.of(
                "total", total,
                "new", newCount,
                "parsed", parsed,
                "synced", synced,
                "failed", failed,
                "manualReview", review
        ));
    }

    @GetMapping("/events/review")
    public ResponseEntity<List<CalendarEvent>> getEventsNeedingReview() {
        return ResponseEntity.ok(calendarEventRepository.findBySyncStatus(CalendarEvent.SyncStatus.MANUAL_REVIEW));
    }

    @GetMapping("/events/failed")
    public ResponseEntity<List<CalendarEvent>> getFailedEvents() {
        return ResponseEntity.ok(calendarEventRepository.findBySyncStatus(CalendarEvent.SyncStatus.FAILED));
    }

    @PostMapping("/renew-channel")
    public ResponseEntity<Void> renewChannel() {
        syncScheduler.renewWatchChannel();
        return ResponseEntity.ok().build();
    }
}