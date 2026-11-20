package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.dto.paymentcalendar.PcBalanceRequestDto;
import com.nikulshin.detailing.model.dto.paymentcalendar.PcOperationDto;
import com.nikulshin.detailing.model.dto.paymentcalendar.PcOperationSaveDto;
import com.nikulshin.detailing.model.dto.paymentcalendar.PcPeriodDto;
import com.nikulshin.detailing.model.dto.paymentcalendar.PcSettingsDto;
import com.nikulshin.detailing.service.PaymentCalendarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST API «Платёжного календаря»: движение денег, остатки и кассовые разрывы.
 */
@RestController
@RequestMapping("/api/payment-calendar")
@RequiredArgsConstructor
public class PaymentCalendarController {

    private final PaymentCalendarService paymentCalendarService;

    /** Календарь за месяц: GET /api/payment-calendar?year=2026&month=11 */
    @GetMapping
    public ResponseEntity<PcPeriodDto> getMonth(@RequestParam Integer year, @RequestParam Integer month) {
        return ResponseEntity.ok(paymentCalendarService.getMonth(year, month));
    }

    /** Календарь за неделю, содержащую дату: GET /api/payment-calendar/week?date=2026-11-19 */
    @GetMapping("/week")
    public ResponseEntity<PcPeriodDto> getWeek(@RequestParam LocalDate date) {
        return ResponseEntity.ok(paymentCalendarService.getWeek(date));
    }

    /** Операции за период: GET /api/payment-calendar/operations?start=2026-11-01&end=2026-11-30 */
    @GetMapping("/operations")
    public ResponseEntity<List<PcOperationDto>> getOperations(@RequestParam LocalDate start,
                                                              @RequestParam LocalDate end) {
        return ResponseEntity.ok(paymentCalendarService.getOperations(start, end));
    }

    /** Создать ручную операцию. */
    @PostMapping("/operations")
    public ResponseEntity<PcOperationDto> createOperation(@Valid @RequestBody PcOperationSaveDto dto) {
        return ResponseEntity.ok(paymentCalendarService.createOperation(dto));
    }

    /** Изменить ручную операцию. */
    @PutMapping("/operations/{id}")
    public ResponseEntity<PcOperationDto> updateOperation(@PathVariable Long id,
                                                          @Valid @RequestBody PcOperationSaveDto dto) {
        return ResponseEntity.ok(paymentCalendarService.updateOperation(id, dto));
    }

    /** Удалить ручную операцию. */
    @DeleteMapping("/operations/{id}")
    public ResponseEntity<Void> deleteOperation(@PathVariable Long id) {
        paymentCalendarService.deleteOperation(id);
        return ResponseEntity.ok().build();
    }

    /** Явные стартовые остатки месяца: GET /api/payment-calendar/balances?year=2026&month=11 */
    @GetMapping("/balances")
    public ResponseEntity<Map<String, BigDecimal>> getBalances(@RequestParam Integer year,
                                                               @RequestParam Integer month) {
        return ResponseEntity.ok(paymentCalendarService.getExplicitStartingBalances(year, month));
    }

    /** Сохранить стартовый остаток счёта на месяц. */
    @PostMapping("/balances")
    public ResponseEntity<Void> saveBalance(@Valid @RequestBody PcBalanceRequestDto request) {
        paymentCalendarService.saveBalance(request);
        return ResponseEntity.ok().build();
    }

    /** Настройки календаря. */
    @GetMapping("/settings")
    public ResponseEntity<PcSettingsDto> getSettings() {
        return ResponseEntity.ok(paymentCalendarService.getSettings());
    }

    @PutMapping("/settings")
    public ResponseEntity<PcSettingsDto> updateSettings(@Valid @RequestBody PcSettingsDto dto) {
        return ResponseEntity.ok(paymentCalendarService.updateSettings(dto));
    }

    /** Ошибки бизнес-валидации → 400 с понятным сообщением. */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
