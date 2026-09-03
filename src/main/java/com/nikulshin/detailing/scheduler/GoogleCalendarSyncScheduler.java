package com.nikulshin.detailing.scheduler;

import com.google.api.services.calendar.model.Channel;
import com.nikulshin.detailing.configuration.GoogleCalendarProperties;
import com.nikulshin.detailing.service.google.GoogleCalendarService;
import com.nikulshin.detailing.service.google.GoogleCalendarSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarSyncScheduler {

    private final GoogleCalendarProperties properties;
    private final GoogleCalendarService googleCalendarService;
    private final GoogleCalendarSyncService syncService;

    private String currentChannelId;
    private String currentResourceId;

    @Scheduled(initialDelayString = "${google.calendar.sync.initial-delay:60000}", fixedRateString = "${google.calendar.sync.poll-interval:3600000}")
    public void scheduledSync() {
        if (!properties.getSync().isEnabled()) {
            return;
        }

        log.info("Starting scheduled Google Calendar sync");
        LocalDateTime timeMin = LocalDateTime.now().minus(properties.getSync().getInitialLookback());
        LocalDateTime timeMax = LocalDateTime.now().plus(properties.getSync().getLookahead());

        try {
            var result = syncService.syncEvents(timeMin, timeMax);
            log.info("Sync completed: created={}, updated={}, synced={}, failed={}, needsReview={}",
                    result.getCreated(), result.getUpdated(), result.getSynced(), result.getFailed(), result.getNeedsReview());
        } catch (Exception e) {
            log.error("Scheduled sync failed: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRateString = "${google.calendar.sync.channel-renewal-interval:7200000}")
    public void renewWatchChannel() {
        if (!properties.getSync().isEnabled() || properties.getWebhookBaseUrl() == null) {
            return;
        }

        try {
            if (currentChannelId != null && currentResourceId != null) {
                googleCalendarService.stopChannel(currentChannelId, currentResourceId);
                log.info("Stopped old watch channel: {}", currentChannelId);
            }

            String channelId = "detailing-calendar-" + System.currentTimeMillis();
            String webhookUrl = properties.getWebhookBaseUrl() + properties.getWebhookPath();
            LocalDateTime timeMin = LocalDateTime.now().minusDays(1);

            Channel channel = googleCalendarService.watchEvents(channelId, webhookUrl, timeMin);
            currentChannelId = channel.getId();
            currentResourceId = channel.getResourceId();

            log.info("Renewed watch channel: {} (expires: {})", currentChannelId, channel.getExpiration());
        } catch (IOException | GeneralSecurityException e) {
            log.error("Failed to renew watch channel: {}", e.getMessage());
        }
    }

    public void setCurrentChannel(Channel channel) {
        this.currentChannelId = channel.getId();
        this.currentResourceId = channel.getResourceId();
    }

    public void setCurrentChannel(String channelId, String resourceId) {
        this.currentChannelId = channelId;
        this.currentResourceId = resourceId;
    }
}