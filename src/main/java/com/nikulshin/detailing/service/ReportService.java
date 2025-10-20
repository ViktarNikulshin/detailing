package com.nikulshin.detailing.service;

import com.nikulshin.detailing.mapper.ReportMapper;
import com.nikulshin.detailing.model.dto.report.MasterDetailReportDto;
import com.nikulshin.detailing.model.dto.report.MasterWeeklyReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportMapper reportMapper;

    public List<MasterWeeklyReportDto> getWeeklyReport(LocalDateTime start, LocalDateTime end) {
        return reportMapper.getWeeklyReport(start, end);
    }

    public MasterDetailReportDto getMasterDetail(Long id, LocalDateTime start, LocalDateTime end) {
        return reportMapper.getMasterDetailReport(id, start, end);
    }
}
