package com.nikulshin.detailing.service;

import com.nikulshin.detailing.model.domain.CalendarEvent;
import com.nikulshin.detailing.repository.CalendarEventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;

    @Transactional(readOnly = true)
    public List<CalendarEvent> getEventsByDateRange(LocalDate start, LocalDate end) {
        return calendarEventRepository.findByDateBetween(start, end);
    }

    @Transactional(readOnly = true)
    public CalendarEvent getEventById(Long id) {
        return calendarEventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Событие с ID " + id + " не найдено"));
    }

    @Transactional
    public CalendarEvent createEvent(CalendarEvent event) {
        event.setId(null);
        return calendarEventRepository.save(event);
    }

    @Transactional
    public CalendarEvent updateEvent(Long id, CalendarEvent event) {
        CalendarEvent existing = getEventById(id);
        existing.setDate(event.getDate());
        existing.setDescription(event.getDescription());
        return calendarEventRepository.save(existing);
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!calendarEventRepository.existsById(id)) {
            throw new EntityNotFoundException("Событие с ID " + id + " не найдено");
        }
        calendarEventRepository.deleteById(id);
    }
}
