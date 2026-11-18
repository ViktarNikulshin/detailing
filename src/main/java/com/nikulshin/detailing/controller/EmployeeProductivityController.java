package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.dto.report.EmployeeProductivityRecordDto;
import com.nikulshin.detailing.service.EmployeeProductivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API вкладки «Продуктивность сотрудника».
 */
@RestController
@RequestMapping("/api/productivity")
@RequiredArgsConstructor
public class EmployeeProductivityController {

    private final EmployeeProductivityService employeeProductivityService;

    /**
     * Записи продуктивности сотрудника за месяц.
     */
    @GetMapping
    public ResponseEntity<List<EmployeeProductivityRecordDto>> getRecords(
            @RequestParam(name = "employeeId") Long employeeId,
            @RequestParam(name = "year") int year,
            @RequestParam(name = "month") int month) {
        return ResponseEntity.ok(employeeProductivityService.getRecords(employeeId, year, month));
    }

    /**
     * Создание или обновление записи (УСН, налоги и прибыль пересчитываются на бэке).
     */
    @PostMapping
    public ResponseEntity<EmployeeProductivityRecordDto> saveRecord(
            @RequestBody EmployeeProductivityRecordDto recordDto) {
        return ResponseEntity.ok(employeeProductivityService.saveOrUpdate(recordDto));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        employeeProductivityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
