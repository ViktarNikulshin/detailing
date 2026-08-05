package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.domain.DebtRecord;
import com.nikulshin.detailing.service.DebtRecordService;
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

@RestController
@RequestMapping("/api/debt")
@RequiredArgsConstructor
public class DebtRecordController {

    private final DebtRecordService debtRecordService;

    @GetMapping
    public ResponseEntity<List<DebtRecord>> getActsSummary(
            @RequestParam(name = "year") int year,
            @RequestParam(name = "month") int month) {
        List<DebtRecord> acts = debtRecordService.getDebtsSummary(year, month);
        return ResponseEntity.ok(acts);
    }

    @PostMapping
    public ResponseEntity<DebtRecord> saveActRecord(@RequestBody DebtRecord debtRecord) {
        DebtRecord savedAct = debtRecordService.saveOrUpdateDebt(debtRecord);
        return ResponseEntity.ok(savedAct);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteActRecord(@PathVariable Long id) {
        debtRecordService.deleteDebt(id);
        return ResponseEntity.noContent().build();
    }
}