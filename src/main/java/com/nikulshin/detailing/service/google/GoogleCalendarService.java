package com.nikulshin.detailing.service.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
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
import java.util.Collections;
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

    public List<Event> listEvents(LocalDateTime timeMin, LocalDateTime timeMax) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService();
        String timeMinStr = toRfc3339(timeMin);
        String timeMaxStr = toRfc3339(timeMax);

        Events events = service.events().list(properties.getCalendarId())
                .setTimeMin(com.google.api.client.util.DateTime.parseRfc3339(timeMinStr))
                .setTimeMax(com.google.api.client.util.DateTime.parseRfc3339(timeMaxStr))
                .setMaxResults(properties.getSync().getMaxResults())
                .setSingleEvents(true)
                .setOrderBy("startTime")
                .execute();

        return events.getItems() != null ? events.getItems() : List.of();
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
}