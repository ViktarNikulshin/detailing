package com.nikulshin.detailing.controller;

import com.google.api.services.calendar.model.Channel;
import com.nikulshin.detailing.scheduler.GoogleCalendarSyncScheduler;
import com.nikulshin.detailing.service.google.GoogleCalendarService;
import com.nikulshin.detailing.service.google.GoogleCalendarSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/google-calendar")
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarWebhookController {

    private final GoogleCalendarService googleCalendarService;
    private final GoogleCalendarSyncService syncService;
    private final GoogleCalendarSyncScheduler syncScheduler;
    private final com.nikulshin.detailing.configuration.GoogleCalendarProperties properties;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
            @RequestHeader(value = "X-Goog-Channel-ID", required = false) String channelId,
            @RequestHeader(value = "X-Goog-Resource-ID", required = false) String resourceId,
            @RequestHeader(value = "X-Goog-Resource-State", required = false) String resourceState,
            @RequestHeader(value = "X-Goog-Message-Number", required = false) String messageNumber
    ) {
        log.info("Received Google Calendar webhook: channelId={}, resourceId={}, resourceState={}, messageNumber={}",
                channelId, resourceId, resourceState, messageNumber);

        if ("sync".equals(resourceState)) {
            log.info("Sync notification received, channel established");
            return ResponseEntity.ok().build();
        }

        if ("exists".equals(resourceState) || "not_exists".equals(resourceState)) {
            String googleEventId = resourceId;
            if (googleEventId != null) {
                syncService.processWebhookEvent(googleEventId, properties.getCalendarId());
            }
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/setup-channel")
    public ResponseEntity<ChannelInfo> setupChannel() {
        try {
            String channelId = "detailing-calendar-" + System.currentTimeMillis();
            String webhookUrl = properties.getWebhookBaseUrl() + properties.getWebhookPath();
            LocalDateTime timeMin = LocalDateTime.now().minusDays(1);

            Channel channel = googleCalendarService.watchEvents(channelId, webhookUrl, timeMin);

            syncScheduler.setCurrentChannel(channel);

            ChannelInfo info = new ChannelInfo();
            info.setChannelId(channel.getId());
            info.setResourceId(channel.getResourceId());
            info.setExpiration(channel.getExpiration());
            info.setWebhookUrl(webhookUrl);

            log.info("Created Google Calendar watch channel: {}", channel.getId());
            return ResponseEntity.ok(info);
        } catch (IOException | GeneralSecurityException e) {
            log.error("Failed to setup watch channel: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/stop-channel")
    public ResponseEntity<Void> stopChannel(@RequestHeader("X-Channel-Id") String channelId,
                                             @RequestHeader("X-Resource-Id") String resourceId) {
        try {
            googleCalendarService.stopChannel(channelId, resourceId);
            return ResponseEntity.ok().build();
        } catch (IOException | GeneralSecurityException e) {
            log.error("Failed to stop channel: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @lombok.Data
    public static class ChannelInfo {
        private String channelId;
        private String resourceId;
        private Long expiration;
        private String webhookUrl;
    }
}