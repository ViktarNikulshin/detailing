package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.dto.report.FinanceMonthSummaryDto;
import com.nikulshin.detailing.model.dto.report.FinanceRecordDto;
import com.nikulshin.detailing.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    // Получить данные за месяц (Начальный баланс + Список строк)
    @GetMapping("/summary")
    public ResponseEntity<FinanceMonthSummaryDto> getSummary(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month) {
        return ResponseEntity.ok(financeService.getMonthSummary(year, month));
    }

    // Сохранить новую или обновить измененную запись
    @PostMapping("/records")
    public ResponseEntity<FinanceRecordDto> saveRecord(@RequestBody FinanceRecordDto dto) {
        return ResponseEntity.ok(financeService.saveRecord(dto));
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<String> deletedRecord(@PathVariable Long id) {
        financeService.deleteRecord(id);
        return ResponseEntity.ok().build();
    }

    // Обновить баланс на начало периода
    @PostMapping("/balance")
    public ResponseEntity<Void> updateBalance(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month,
            @RequestParam("amount") BigDecimal amount) {
        financeService.updateStartingBalance(year, month, amount);
        return ResponseEntity.ok().build();
    }
}