package com.nikulshin.detailing.service.google;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.nikulshin.detailing.configuration.GoogleCalendarProperties;
import com.nikulshin.detailing.model.domain.CalendarEvent;
import com.nikulshin.detailing.model.domain.Client;
import com.nikulshin.detailing.model.domain.Order;
import com.nikulshin.detailing.model.domain.OrderStatus;
import com.nikulshin.detailing.repository.CalendarEventRepository;
import com.nikulshin.detailing.repository.ClientRepository;
import com.nikulshin.detailing.repository.OrderRepository;
import com.nikulshin.detailing.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarSyncService {

    private final GoogleCalendarService googleCalendarService;
    private final EventParserService eventParserService;
    private final CalendarEventRepository calendarEventRepository;
    private final ClientRepository clientRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final GoogleCalendarProperties properties;

    @Transactional
    public SyncResult syncEvents(LocalDateTime timeMin, LocalDateTime timeMax) {
        SyncResult result = new SyncResult();
        try {
            List<Event> events = googleCalendarService.listEvents(timeMin, timeMax);
            log.info("Fetched {} events from Google Calendar", events.size());

            for (Event event : events) {
                try {
                    processEvent(event, result);
                } catch (Exception e) {
                    log.error("Error processing event {}: {}", event.getId(), e.getMessage());
                    result.failed++;
                }
            }
        } catch (Exception e) {
            log.error("Error fetching events from Google Calendar: {}", e.getMessage());
            result.errors.add("Fetch error: " + e.getMessage());
        }
        return result;
    }

    @Transactional
    public void processWebhookEvent(String googleEventId, String calendarId) {
        try {
            Event event = googleCalendarService.getEvent(googleEventId);
            if (event.getStatus() != null && event.getStatus().equals("cancelled")) {
                handleCancelledEvent(googleEventId);
                return;
            }
            SyncResult result = new SyncResult();
            processEvent(event, result);
        } catch (Exception e) {
            log.error("Error processing webhook event {}: {}", googleEventId, e.getMessage());
        }
    }

    private void processEvent(Event event, SyncResult result) {
        Optional<CalendarEvent> existingOpt = calendarEventRepository.findByGoogleEventId(event.getId());

        CalendarEvent calendarEvent = existingOpt.orElse(new CalendarEvent());
        boolean isNew = !existingOpt.isPresent();

        calendarEvent.setGoogleEventId(event.getId());
        calendarEvent.setGoogleCalendarId(properties.getCalendarId());

        if (event.getStart() != null && event.getStart().getDateTime() != null) {
            calendarEvent.setDate(parseDateTime(event.getStart().getDateTime()));
        } else if (event.getStart() != null && event.getStart().getDate() != null) {
            calendarEvent.setDate(LocalDate.parse(event.getStart().getDate().toString()));
        }
        if (event.getEnd() != null && event.getEnd().getDateTime() != null) {
            calendarEvent.setEndDate(parseDateTime(event.getEnd().getDateTime()));
        } else if (event.getEnd() != null && event.getEnd().getDate() != null) {
            calendarEvent.setEndDate(LocalDate.parse(event.getEnd().getDate().toString()));
        }

        calendarEvent.setDescription(event.getDescription() != null ? event.getDescription() : "");

        EventParserService.ParsedEvent parsed = eventParserService.parseWithLlm(calendarEvent.getDescription());

        calendarEvent.setParsedName(parsed.getName());
        calendarEvent.setParsedPhone(parsed.getPhone());
        calendarEvent.setParsedCarBrand(parsed.getCar());
        calendarEvent.setParsedComment(parsed.getComment());
        calendarEvent.setLastSyncedAt(LocalDateTime.now());

        if (parsed.getConfidence() >= 0.7 && parsed.getName() != null && parsed.getPhone() != null) {
            calendarEvent.setSyncStatus(CalendarEvent.SyncStatus.PARSED);

            if (isNew || calendarEvent.getCreatedOrderId() == null) {
                try {
                    Order createdOrder = createOrderFromParsed(calendarEvent, parsed);
                    calendarEvent.setCreatedOrderId(createdOrder.getId());
                    calendarEvent.setSyncStatus(CalendarEvent.SyncStatus.SYNCED);
                    result.synced++;
                } catch (Exception e) {
                    log.error("Failed to create order for event {}: {}", event.getId(), e.getMessage());
                    calendarEvent.setSyncStatus(CalendarEvent.SyncStatus.FAILED);
                    calendarEvent.setSyncError(e.getMessage());
                    result.failed++;
                }
            } else {
                result.synced++;
            }
        } else {
            calendarEvent.setSyncStatus(CalendarEvent.SyncStatus.MANUAL_REVIEW);
            calendarEvent.setSyncError("Low confidence or missing required fields (confidence: " + parsed.getConfidence() + ")");
            result.needsReview++;
        }

        calendarEventRepository.save(calendarEvent);

        if (isNew) result.created++;
        else result.updated++;
    }

    private Order createOrderFromParsed(CalendarEvent calendarEvent, EventParserService.ParsedEvent parsed) {
        String cleanPhone = parsed.getPhone().replaceAll("[^\\d+]", "");

        Client client = clientRepository.findByPhone(cleanPhone)
                .orElseGet(() -> {
                    Client newClient = new Client();
                    newClient.setName(parsed.getName());
                    newClient.setPhone(cleanPhone);
                    newClient.setCreatedAt(LocalDateTime.now());
                    return clientRepository.save(newClient);
                });

        if (parsed.getName() != null && !parsed.getName().equals(client.getName())) {
            client.setName(parsed.getName());
            clientRepository.save(client);
        }

        Order order = new Order();
        order.setClient(client);
        order.setCarBrand(parsed.getCar());
        order.setStatus(OrderStatus.NEW);
        order.setCreatedAt(LocalDateTime.now());
        order.setArrivalDate(calendarEvent.getDate().atStartOfDay());
        order.setDeliveryDate(calendarEvent.getEndDate().atStartOfDay());

        String comment = parsed.getComment();
        if (parsed.getCar() != null) {
            comment = (comment != null ? comment + " " : "") + "Модель: " + parsed.getCar();
        }

        return orderRepository.save(order);
    }

    private void handleCancelledEvent(String googleEventId) {
        calendarEventRepository.findByGoogleEventId(googleEventId).ifPresent(calendarEvent -> {
            calendarEvent.setSyncStatus(CalendarEvent.SyncStatus.FAILED);
            calendarEvent.setSyncError("Event cancelled in Google Calendar");
            calendarEventRepository.save(calendarEvent);

            if (calendarEvent.getCreatedOrderId() != null) {
                orderRepository.findById(calendarEvent.getCreatedOrderId()).ifPresent(order -> {
                    order.setStatus(OrderStatus.CANCELLED);
                    orderRepository.save(order);
                });
            }
        });
    }

    private LocalDate parseDateTime(com.google.api.client.util.DateTime dateTime) {
        if (dateTime == null) return null;
        try {
            return LocalDate.ofInstant(
                    java.time.Instant.ofEpochMilli(dateTime.getValue()),
                    ZoneId.systemDefault()
            );
        } catch (Exception e) {
            log.warn("Failed to parse dateTime: {}", dateTime.toStringRfc3339());
            return LocalDate.now();
        }
    }

    @lombok.Data
    public static class SyncResult {
        private int created = 0;
        private int updated = 0;
        private int synced = 0;
        private int failed = 0;
        private int needsReview = 0;
        private List<String> errors = new java.util.ArrayList<>();
    }
}