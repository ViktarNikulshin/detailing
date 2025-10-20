package com.nikulshin.detailing.controller;


import com.nikulshin.detailing.model.dto.report.MasterDetailEarningDto;
import com.nikulshin.detailing.model.dto.report.MasterDetailReportDto;
import com.nikulshin.detailing.model.dto.report.MasterWeeklyReportDto;
import com.nikulshin.detailing.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
