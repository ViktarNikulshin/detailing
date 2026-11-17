package com.nikulshin.detailing.service.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Channel;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.nikulshin.detailing.configuration.GoogleCalendarProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService {

    private final GoogleCalendarProperties properties;
    private final GoogleOAuthService oauthService;

    private Calendar getCalendarService() throws IOException, GeneralSecurityException {
        Credential credential = oauthService.getCredential();

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("Detailing Calendar Sync")
                .build();
    }

    /**
     * Lists calendar events, incrementally when a valid {@code syncToken} is supplied.
     *
     * <p>When {@code syncToken} is non-null the API returns only events changed since the
     * token was issued and a fresh {@code nextSyncToken}; in that case {@code timeMin}/{@code timeMax}
     * are ignored (Google forbids combining them with a sync token). Otherwise a full listing
     * for the given window is performed.
     */
    public ListEventsResult listEvents(LocalDateTime timeMin, LocalDateTime timeMax, String syncToken)
            throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService();

        Calendar.Events.List request = service.events().list(properties.getCalendarId())
                .setMaxResults(properties.getSync().getMaxResults())
                .setSingleEvents(true);

        if (syncToken != null && !syncToken.isEmpty()) {
            request.setSyncToken(syncToken)
                    .setShowDeleted(true);
        } else {
            request.setTimeMin(com.google.api.client.util.DateTime.parseRfc3339(toRfc3339(timeMin)))
                    .setTimeMax(com.google.api.client.util.DateTime.parseRfc3339(toRfc3339(timeMax)))
                    .setOrderBy("startTime");
        }

        Events events = request.execute();

        List<Event> items = events.getItems() != null ? events.getItems() : List.of();
        return new ListEventsResult(items, events.getNextSyncToken());
    }

    public Channel watchEvents(String channelId, String webhookUrl, LocalDateTime timeMin) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService();

        Channel channel = new Channel()
                .setId(channelId)
                .setType("web_hook")
                .setAddress(webhookUrl)
                .setToken(UUID.randomUUID().toString())
                .setExpiration(Instant.now().plusSeconds(24 * 60 * 60).toEpochMilli()); // 24 hours

        return service.events().watch(properties.getCalendarId(), channel)
                .setTimeMin(com.google.api.client.util.DateTime.parseRfc3339(toRfc3339(timeMin)))
                .execute();
    }

    public void stopChannel(String channelId, String resourceId) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService();
        Channel channel = new Channel().setId(channelId).setResourceId(resourceId);
        service.channels().stop(channel).execute();
    }

    public Event getEvent(String eventId) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService();
        return service.events().get(properties.getCalendarId(), eventId).execute();
    }

    private String toRfc3339(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public record ListEventsResult(List<Event> events, String nextSyncToken) {
    }
}