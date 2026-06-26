package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.dto.report.*;
import com.nikulshin.detailing.service.LandlordReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports/landlord")
@RequiredArgsConstructor
public class LandRestController {

    private final LandlordReconciliationService reconciliationService;

    // Получить полную сводку за выбранный месяц
    @GetMapping
    public ResponseEntity<MonthlyReconciliationDto> getSummary(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(reconciliationService.getMonthlySummary(year, month));
    }

    // Сохранить/обновить показатели "остаток" и "доп соглашение" (триггерится по onBlur во фронтенде)
    @PutMapping("/meta")
    public ResponseEntity<Void> updateMeta(@RequestBody UpdateMetaRequest request) {
        reconciliationService.updateMonthlyMeta(request);
        return ResponseEntity.ok().build();
    }

    // Добавить или обновить строку услуги в таблице
    @PostMapping("/records")
    public ResponseEntity<Void> saveRecord(@RequestBody SaveRecordRequest request) {
        reconciliationService.saveOrUpdateRecord(request);
        return ResponseEntity.ok().build();
    }

    // Удалить строку услуги
    @DeleteMapping("/records/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        reconciliationService.deleteRecord(id);
        return ResponseEntity.ok().build();
    }
}