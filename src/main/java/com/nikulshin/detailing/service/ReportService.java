package com.nikulshin.detailing.service;

import com.nikulshin.detailing.mapper.MasterSalaryMapper;
import com.nikulshin.detailing.mapper.ReportMapper;
import com.nikulshin.detailing.model.domain.MasterSalaryBalance;
import com.nikulshin.detailing.model.domain.MasterSalaryLog;
import com.nikulshin.detailing.model.domain.User;
import com.nikulshin.detailing.model.dto.report.MasterDetailReportDto;
import com.nikulshin.detailing.model.dto.report.MasterSalaryDto;
import com.nikulshin.detailing.model.dto.report.MasterSalaryRecordDto;
import com.nikulshin.detailing.model.dto.report.MasterWeeklyReportDto;
import com.nikulshin.detailing.repository.MasterSalaryBalanceRepository;
import com.nikulshin.detailing.repository.MasterSalaryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportMapper reportMapper;
    private final MasterSalaryMapper masterSalaryMapper;
    private final MasterSalaryRepository masterSalaryRepository;
    private final UserService userService;
    private final MasterSalaryBalanceRepository masterSalaryBalanceRepository;

    public List<MasterWeeklyReportDto> getWeeklyReport(LocalDateTime start, LocalDateTime end) {
        return reportMapper.getWeeklyReport(start, end);
    }

    public MasterDetailReportDto getMasterDetail(Long id, LocalDateTime start, LocalDateTime end) {
        return reportMapper.getMasterDetailReport(id, start, end);
    }

    public MasterSalaryDto getMasterSalaryLog(Long id, LocalDateTime start, LocalDateTime end) {
        User master = userService.getById(id);
        List<MasterSalaryRecordDto> records = masterSalaryMapper
                .domainsToDtos(masterSalaryRepository.findAllByMasterIsAndDateAfterAndDateBefore(master, start, end))
                .stream()
                .sorted(Comparator.comparing(MasterSalaryRecordDto::getDate))
                .toList();
        BigDecimal previousBalance = masterSalaryBalanceRepository
                .findByMasterAndYearAndMonth(master, start.getYear(), start.getMonthValue())
                .map(MasterSalaryBalance::getPreviousBalance)
                .orElse(new BigDecimal(0));

        return MasterSalaryDto.builder()
                .previousBalance(previousBalance)
                .records(records)
                .build();
    }

    public void saveMasterSalary(MasterSalaryRecordDto masterSalaryDto) {
        User master = userService.getById(masterSalaryDto.getMasterId());
        MasterSalaryLog masterSalaryLog = masterSalaryMapper.dtoToDomain(masterSalaryDto);
        masterSalaryLog.setMaster(master);
        masterSalaryRepository.save(masterSalaryLog);
    }

    public void deleteMasterSalary(Long id) {
        masterSalaryRepository.deleteById(id);
    }

    public void updateMasterSalary(Long id, MasterSalaryRecordDto masterSalaryDto) {
        User master = userService.getById(masterSalaryDto.getMasterId());
        MasterSalaryLog masterSalaryLog = masterSalaryMapper.dtoToDomain(masterSalaryDto);
        masterSalaryLog.setMaster(master);
        masterSalaryLog.setId(id);
        masterSalaryRepository.save(masterSalaryLog);

    }
    @Transactional
    public void updateMasterBalance(Long id, Integer year, Integer month, BigDecimal previousBalance, BigDecimal interimPayments) {
        User master = userService.getById(id);
        Optional<MasterSalaryBalance> balance = masterSalaryBalanceRepository
                .findByMasterAndYearAndMonth(master, year, month);
        if (balance.isPresent()) {
            MasterSalaryBalance salaryBalance = balance.get();
            salaryBalance.setPreviousBalance(previousBalance);
            salaryBalance.setInterimPayments(interimPayments);
        } else {
            MasterSalaryBalance newBalance = new MasterSalaryBalance();
            newBalance.setMaster(master);
            newBalance.setYear(year);
            newBalance.setMonth(month);
            newBalance.setPreviousBalance(previousBalance);
            newBalance.setInterimPayments(interimPayments);
            masterSalaryBalanceRepository.save(newBalance);
        }
    }
}
