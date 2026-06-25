package com.nikulshin.detailing.controller;


import com.nikulshin.detailing.model.dto.report.MasterDetailReportDto;
import com.nikulshin.detailing.model.dto.report.MasterSalaryDto;
import com.nikulshin.detailing.model.dto.report.MasterSalaryRecordDto;
import com.nikulshin.detailing.model.dto.report.MasterWeeklyReportDto;
import com.nikulshin.detailing.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/masters-weekly")
    public ResponseEntity<List<MasterWeeklyReportDto>> getWeeklyReport(@RequestParam LocalDateTime start,
                                                                       @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(reportService.getWeeklyReport(start, end));
    }

    @GetMapping("/master-detail/{id}")
    public ResponseEntity<MasterDetailReportDto> getWeeklyReport(@PathVariable Long id,
                                                                 @RequestParam LocalDateTime start,
                                                                 @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(reportService.getMasterDetail(id, start, end));
    }

    @GetMapping("/masters-salary-log")
    public ResponseEntity<MasterSalaryDto> getMasterSalaryLog(@RequestParam Long id,
                                                              @RequestParam LocalDateTime start,
                                                              @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(reportService.getMasterSalaryLog(id, start, end));
    }

    @PostMapping("/masters-salary-log")
    public ResponseEntity<String> getMasterSalaryLog(@RequestBody MasterSalaryRecordDto masterSalaryDto) {
        reportService.saveMasterSalary(masterSalaryDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/masters-salary-log/{id}")
    public ResponseEntity<String> deleteMasterSalaryLog(@PathVariable Long id) {
        reportService.deleteMasterSalary(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/masters-salary-log/{id}")
    public ResponseEntity<String> updateMasterSalaryLog(@PathVariable Long id, @RequestBody MasterSalaryRecordDto masterSalaryDto) {
        reportService.updateMasterSalary(id, masterSalaryDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/masters-salary-balance/")
    public ResponseEntity<String> updateMasterSalaryLog(@RequestParam Long id,
                                                        @RequestParam Integer year,
                                                        @RequestParam Integer month,
                                                        @RequestParam BigDecimal previousBalance,
                                                        @RequestParam(name = "interimPayments", required = false) BigDecimal interimPayments) {
        reportService.updateMasterBalance(id, year, month, previousBalance, interimPayments);
        return ResponseEntity.ok().build();
    }
}
