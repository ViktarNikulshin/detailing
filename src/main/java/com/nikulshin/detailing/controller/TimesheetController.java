package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.dto.report.TimesheetSaveRequest;
import com.nikulshin.detailing.model.dto.report.UserTimesheetDto;
import com.nikulshin.detailing.service.TimesheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports/timesheet")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Настройте CORS под ваш проект
public class TimesheetController {

    private final TimesheetService timesheetService;

    // GET /api/reports/timesheet?masterId=1&month=2026-06
    @GetMapping
    public ResponseEntity<UserTimesheetDto> getTimesheet(
            @RequestParam Long masterId,
            @RequestParam String month) {

        UserTimesheetDto response = timesheetService.getTimesheet(masterId, month);
        return ResponseEntity.ok(response);
    }

    // POST /api/reports/timesheet
    @PostMapping
    public ResponseEntity<String> saveTimesheet(@RequestBody TimesheetSaveRequest request) {
        timesheetService.saveTimesheet(request);
        return ResponseEntity.ok("Табель успешно сохранен");
    }
}
