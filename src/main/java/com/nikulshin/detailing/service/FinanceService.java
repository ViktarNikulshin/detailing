package com.nikulshin.detailing.service;

import com.nikulshin.detailing.model.domain.FinanceBalance;
import com.nikulshin.detailing.model.domain.FinanceRecord;
import com.nikulshin.detailing.model.dto.report.FinanceMonthSummaryDto;
import com.nikulshin.detailing.model.dto.report.FinanceRecordDto;
import com.nikulshin.detailing.repository.FinanceBalanceRepository;
import com.nikulshin.detailing.repository.FinanceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final FinanceRecordRepository recordRepository;
    private final FinanceBalanceRepository balanceRepository;

    @Transactional(readOnly = true)
    public FinanceMonthSummaryDto getMonthSummary(Integer year, Integer month) {
        // 1. Получаем начальный баланс
        BigDecimal startingBalance = balanceRepository.findByYearAndMonth(year, month)
                .map(FinanceBalance::getStartingBalance)
                .orElse(BigDecimal.ZERO); // Если записи нет, по дефолту 0 (можно сделать логику переноса из прошлого месяца)

        // 2. Получаем все записи за месяц
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<FinanceRecordDto> records = recordRepository.findByMonthRange(start, end)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new FinanceMonthSummaryDto(startingBalance, records);
    }

    @Transactional
    public void updateStartingBalance(Integer year, Integer month, BigDecimal balanceAmount) {
        FinanceBalance balance = balanceRepository.findByYearAndMonth(year, month)
                .orElse(new FinanceBalance());

        balance.setYear(year);
        balance.setMonth(month);
        balance.setStartingBalance(balanceAmount);

        balanceRepository.save(balance);
    }

    @Transactional
    public FinanceRecordDto saveRecord(FinanceRecordDto dto) {
        FinanceRecord record = (dto.getId() != null)
                ? recordRepository.findById(dto.getId()).orElse(new FinanceRecord())
                : new FinanceRecord();

        record.setRecordDate(dto.getDate());
        record.setIncome(dto.getIncome());
        record.setExpUtilities(dto.getExpUtilities());
        record.setExpRent(dto.getExpRent());
        record.setExpMaterials(dto.getExpMaterials());
        record.setExpCredit(dto.getExpCredit());
        record.setExpSalary(dto.getExpSalary());
        record.setExpTaxes(dto.getExpTaxes());
        record.setExpOther(dto.getExpOther());

        FinanceRecord saved = recordRepository.save(record);
        return convertToDto(saved);
    }

    private FinanceRecordDto convertToDto(FinanceRecord entity) {
        FinanceRecordDto dto = new FinanceRecordDto();
        dto.setId(entity.getId());
        dto.setDate(entity.getRecordDate());
        dto.setIncome(entity.getIncome());
        dto.setExpUtilities(entity.getExpUtilities());
        dto.setExpRent(entity.getExpRent());
        dto.setExpMaterials(entity.getExpMaterials());
        dto.setExpCredit(entity.getExpCredit());
        dto.setExpSalary(entity.getExpSalary());
        dto.setExpTaxes(entity.getExpTaxes());
        dto.setExpOther(entity.getExpOther());
        return dto;
    }
}