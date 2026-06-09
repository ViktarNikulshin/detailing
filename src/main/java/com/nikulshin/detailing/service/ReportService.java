package com.nikulshin.detailing.service;

import com.nikulshin.detailing.mapper.MasterSalaryMapper;
import com.nikulshin.detailing.mapper.ReportMapper;
import com.nikulshin.detailing.model.domain.Dictionary;
import com.nikulshin.detailing.model.domain.MasterSalaryLog;
import com.nikulshin.detailing.model.domain.User;
import com.nikulshin.detailing.model.dto.report.MasterDetailReportDto;
import com.nikulshin.detailing.model.dto.report.MasterSalaryDto;
import com.nikulshin.detailing.model.dto.report.MasterWeeklyReportDto;
import com.nikulshin.detailing.repository.MasterSalaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportMapper reportMapper;
    private final MasterSalaryMapper masterSalaryMapper;
    private final MasterSalaryRepository masterSalaryRepository;
    private final UserService userService;
    private final DictionaryService dictionaryService;

    public List<MasterWeeklyReportDto> getWeeklyReport(LocalDateTime start, LocalDateTime end) {
        return reportMapper.getWeeklyReport(start, end);
    }

    public MasterDetailReportDto getMasterDetail(Long id, LocalDateTime start, LocalDateTime end) {
        return reportMapper.getMasterDetailReport(id, start, end);
    }

    public List<MasterSalaryDto> getMasterSalaryLog(Long id, LocalDateTime start, LocalDateTime end) {
        User master = userService.getById(id);
        return masterSalaryMapper.domainsToDtos(masterSalaryRepository.findAllByMasterIsAndDateAfterAndDateBefore(master, start, end));
    }

    public void saveMasterSalary(MasterSalaryDto masterSalaryDto) {
        User master = userService.getById(masterSalaryDto.getMasterId());
        Dictionary dictionary = dictionaryService.getDictionaryEntityById(masterSalaryDto.getWorkTypeId());
        MasterSalaryLog masterSalaryLog = masterSalaryMapper.dtoToDomain(masterSalaryDto);
        masterSalaryLog.setMaster(master);
        masterSalaryLog.setWorkType(dictionary);
        masterSalaryRepository.save(masterSalaryLog);
    }

    public void deleteMasterSalary(Long id) {
        masterSalaryRepository.deleteById(id);
    }

    public void updateMasterSalary(Long id, MasterSalaryDto masterSalaryDto) {
        User master = userService.getById(masterSalaryDto.getMasterId());
        Dictionary dictionary = dictionaryService.getDictionaryEntityById(masterSalaryDto.getWorkTypeId());
        MasterSalaryLog masterSalaryLog = masterSalaryMapper.dtoToDomain(masterSalaryDto);
        masterSalaryLog.setMaster(master);
        masterSalaryLog.setWorkType(dictionary);
        masterSalaryLog.setId(id);
        masterSalaryRepository.save(masterSalaryLog);

    }
}
