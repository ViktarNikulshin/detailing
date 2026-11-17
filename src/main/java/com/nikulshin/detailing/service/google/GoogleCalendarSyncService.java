package com.nikulshin.detailing.service.google;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.calendar.model.Event;
import com.nikulshin.detailing.configuration.GoogleCalendarProperties;
import com.nikulshin.detailing.model.domain.CalendarEvent;
import com.nikulshin.detailing.model.domain.CalendarSyncState;
import com.nikulshin.detailing.model.domain.Client;
import com.nikulshin.detailing.model.domain.Dictionary;
import com.nikulshin.detailing.model.domain.Order;
import com.nikulshin.detailing.model.domain.OrderStatus;
import com.nikulshin.detailing.model.domain.Work;
import com.nikulshin.detailing.repository.CalendarEventRepository;
import com.nikulshin.detailing.repository.CalendarSyncStateRepository;
import com.nikulshin.detailing.repository.ClientRepository;
import com.nikulshin.detailing.repository.DictionaryRepository;
import com.nikulshin.detailing.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarSyncService {

    private final GoogleCalendarService googleCalendarService;
    private final EventParserService eventParserService;
    private final CalendarEventRepository calendarEventRepository;
    private final CalendarSyncStateRepository syncStateRepository;
    private final ClientRepository clientRepository;
    private final DictionaryRepository dictionaryRepository;
    private final OrderRepository orderRepository;
    private final GoogleCalendarProperties properties;

    @Transactional
    public SyncResult syncEvents(LocalDateTime timeMin, LocalDateTime timeMax) {
        SyncResult result = new SyncResult();
        try {
            String syncToken = syncStateRepository.findById(CalendarSyncState.SINGLETON_ID)
                    .map(CalendarSyncState::getNextSyncToken)
                    .orElse(null);

            GoogleCalendarService.ListEventsResult listResult;
            try {
                listResult = googleCalendarService.listEvents(timeMin, timeMax, syncToken);
            } catch (GoogleJsonResponseException e) {
                if (e.getStatusCode() == 410) {
                    log.warn("Sync token expired or invalid, falling back to full sync");
                    listResult = googleCalendarService.listEvents(timeMin, timeMax, null);
                } else {
                    throw e;
                }
            }

            List<Event> events = listResult.events();
            log.info("Fetched {} events from Google Calendar (incremental={})",
                    events.size(), syncToken != null && !syncToken.isEmpty());

            for (Event event : events) {
                try {
                    processEvent(event, result);
                } catch (Exception e) {
                    log.error("Error processing event {}: {}", event.getId(), e.getMessage());
                    result.failed++;
                }
            }

            if (listResult.nextSyncToken() != null) {
                CalendarSyncState state = syncStateRepository.findById(CalendarSyncState.SINGLETON_ID)
                        .orElse(new CalendarSyncState());
                state.setId(CalendarSyncState.SINGLETON_ID);
                state.setNextSyncToken(listResult.nextSyncToken());
                state.setUpdatedAt(LocalDateTime.now());
                syncStateRepository.save(state);
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
        if ("cancelled".equals(event.getStatus())) {
            handleCancelledEvent(event.getId());
            result.cancelled++;
            return;
        }

        Optional<CalendarEvent> existingOpt = calendarEventRepository.findByGoogleEventId(event.getId());

        // Idempotency guard: skip unchanged, already-attempted events to avoid re-burning LLM tokens.
        if (existingOpt.isPresent()
                && existingOpt.get().getGoogleEventEtag() != null
                && Objects.equals(existingOpt.get().getGoogleEventEtag(), event.getEtag())) {
            result.skipped++;
            return;
        }

        CalendarEvent calendarEvent = existingOpt.orElse(new CalendarEvent());
        boolean isNew = existingOpt.isEmpty();

        calendarEvent.setGoogleEventId(event.getId());
        calendarEvent.setGoogleCalendarId(properties.getCalendarId());
        calendarEvent.setGoogleEventEtag(event.getEtag());

        if (event.getStart() != null && event.getStart().getDateTime() != null) {
            calendarEvent.setDate(parseDateTime(event.getStart().getDateTime()));
        } else if (event.getStart() != null && event.getStart().getDate() != null) {
            calendarEvent.setDate(parseAllDayDate(event.getStart().getDate().toString()));
        }
        if (event.getEnd() != null && event.getEnd().getDateTime() != null) {
            calendarEvent.setEndDate(parseDateTime(event.getEnd().getDateTime()));
        } else if (event.getEnd() != null && event.getEnd().getDate() != null) {
            calendarEvent.setEndDate(parseAllDayDate(event.getEnd().getDate().toString()));
        }

        // Для извлечения работ и стоимости передаём в парсер и название, и описание:
        // на практике цена/услуга часто указывается именно в заголовке события.
        calendarEvent.setDescription(buildEventText(event));

        // Телефон определяет, является ли событие заказом. LLM вызывается всегда:
        // при найденных regex именe/телефоне — для работ и обогащения,
        // иначе — для полного разбора записи.
        EventParserService.ParsedEvent parsed = eventParserService.parseForOrder(calendarEvent.getDescription());

        calendarEvent.setParsedName(parsed.getName());
        calendarEvent.setParsedPhone(parsed.getPhone());
        calendarEvent.setParsedCarBrand(parsed.getCar());
        calendarEvent.setParsedComment(parsed.getComment());
        calendarEvent.setLastSyncedAt(LocalDateTime.now());

        // Наличие телефона — единственный обязательный признак заказа.
        // Имя может быть неизвестно: Client.name обязательное, поэтому используем
        // понятное техническое значение и позволяем оператору исправить его позже.
        if (parsed.getPhone() != null && !parsed.getPhone().isBlank()) {
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
            calendarEvent.setSyncError("Phone number was not found in event description");
            result.needsReview++;
        }

        calendarEventRepository.save(calendarEvent);

        if (isNew) result.created++;
        else result.updated++;
    }

    private String buildEventText(Event event) {
        String summary = event.getSummary() == null ? "" : event.getSummary().trim();
        String description = event.getDescription() == null ? "" : event.getDescription().trim();

        if (summary.isEmpty()) {
            return description;
        }
        if (description.isEmpty()) {
            return summary;
        }
        return summary + "\nОписание события:\n" + description;
    }

    private Order createOrderFromParsed(CalendarEvent calendarEvent, EventParserService.ParsedEvent parsed) {
        String cleanPhone = parsed.getPhone().replaceAll("[^\\d+]", "");
        String clientName = parsed.getName() == null || parsed.getName().isBlank()
                ? "Клиент без имени"
                : parsed.getName();

        Client client = clientRepository.findByPhone(cleanPhone)
                .orElseGet(() -> {
                    Client newClient = new Client();
                    newClient.setName(clientName);
                    newClient.setPhone(cleanPhone);
                    newClient.setCreatedAt(LocalDateTime.now());
                    return clientRepository.save(newClient);
                });

        if (parsed.getName() != null && !parsed.getName().isBlank()
                && !parsed.getName().equals(client.getName())) {
            client.setName(parsed.getName());
            clientRepository.save(client);
        }

        Order order = new Order();
        order.setClient(client);
        order.setCarBrand(parsed.getCar());
        order.setStatus(OrderStatus.NEW);
        order.setCreatedAt(LocalDateTime.now());
        order.setArrivalDate(calendarEvent.getDate());
        order.setDeliveryDate(calendarEvent.getEndDate());

        // Если LLM не распознал ни одной работы, сохраняем весь исходный текст,
        // чтобы оператор не потерял детали записи календаря.
        String comment = parsed.getWorks().isEmpty()
                ? calendarEvent.getDescription()
                : parsed.getComment();
        if (parsed.getCar() != null && !parsed.getCar().isBlank()) {
            comment = (comment != null && !comment.isBlank() ? comment + " " : "")
                    + "Модель: " + parsed.getCar();
        }
        order.setComment(comment);

        List<Dictionary> activeWorkTypes = dictionaryRepository.findByTypeAndIsActiveTrue("WORK_TYPE");
        for (EventParserService.ParsedWork parsedWork : parsed.getWorks()) {
            activeWorkTypes.stream()
                    .filter(dictionary -> matchesWorkType(dictionary, parsedWork.type()))
                    .findFirst()
                    .ifPresent(dictionary -> {
                        Work work = new Work();
                        work.setOrder(order);
                        work.setWorkType(dictionary);
                        work.setCost(parsedWork.cost());
                        order.getWorks().add(work);
                    });
        }

        // LLM мог вернуть типы, которых нет в актуальном справочнике.
        // В этом случае сохраняем полный description, а не частичный комментарий.
        if (order.getWorks().isEmpty()) {
            order.setComment(calendarEvent.getDescription());
        }

        return orderRepository.save(order);
    }

    private boolean matchesWorkType(Dictionary dictionary, String llmType) {
        String search = llmType.trim().toLowerCase();
        return (dictionary.getCode() != null && dictionary.getCode().trim().equalsIgnoreCase(search))
                || (dictionary.getName() != null && dictionary.getName().trim().equalsIgnoreCase(search));
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

    private LocalDateTime parseDateTime(com.google.api.client.util.DateTime dateTime) {
        if (dateTime == null) return null;
        try {
            return LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(dateTime.getValue()),
                    ZoneId.systemDefault()
            );
        } catch (Exception e) {
            log.warn("Failed to parse dateTime: {}", dateTime.toStringRfc3339());
            return LocalDateTime.now();
        }
    }

    private LocalDateTime parseAllDayDate(String date) {
        return LocalDate.parse(date).atStartOfDay();
    }

    @lombok.Data
    public static class SyncResult {
        private int created = 0;
        private int updated = 0;
        private int synced = 0;
        private int failed = 0;
        private int needsReview = 0;
        private int skipped = 0;
        private int cancelled = 0;
        private List<String> errors = new java.util.ArrayList<>();
    }
}