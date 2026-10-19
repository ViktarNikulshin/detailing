package com.nikulshin.detailing.service;

import com.nikulshin.detailing.model.domain.FinanceBalance;
import com.nikulshin.detailing.model.domain.FinanceRecord;
import com.nikulshin.detailing.model.domain.FinanceType;
import com.nikulshin.detailing.model.dto.report.FinanceBalanceRequest;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final FinanceRecordRepository recordRepository;
    private final FinanceBalanceRepository balanceRepository;

    @Transactional(readOnly = true)
    public FinanceMonthSummaryDto getMonthSummary(Integer year, Integer month, FinanceType type) {
        // 1. Получаем начальный баланс
        BigDecimal startingBalance = balanceRepository.findByYearAndMonthAndFinanceType(year, month, type)
                .map(FinanceBalance::getStartingBalance)
                .orElse(BigDecimal.ZERO); // Если записи нет, по дефолту 0 (можно сделать логику переноса из прошлого месяца)

        // 2. Получаем все записи за месяц
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<FinanceRecordDto> records = recordRepository.findByMonthRange(start, end, type)
                .stream()
                .map(this::convertToDto)
                .sorted(Comparator.comparing(FinanceRecordDto::getDate))
                .collect(Collectors.toList());

        return new FinanceMonthSummaryDto(startingBalance, records);
    }

    @Transactional
    public void updateStartingBalance(FinanceBalanceRequest financeBalanceRequest) {
        FinanceType type = FinanceType.valueOf(financeBalanceRequest.getType());
        FinanceBalance balance = balanceRepository.findByYearAndMonthAndFinanceType(
                        financeBalanceRequest.getYear(),
                        financeBalanceRequest.getMonth(),
                        type)
                .orElse(new FinanceBalance());

        balance.setYear(financeBalanceRequest.getYear());
        balance.setMonth(financeBalanceRequest.getMonth());
        balance.setStartingBalance(financeBalanceRequest.getAmount());
        balance.setFinanceType(type);

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
        record.setExpAdvertising(dto.getExpAdvertising());
        record.setExpPartners(dto.getExpPartners());
        record.setExpOther(dto.getExpOther());
        record.setExpCurrentAccount(dto.getExpCurrentAccount());
        record.setFinanceType(dto.getFinanceType());

        FinanceRecord saved = recordRepository.save(record);

        syncCurrentAccountIncome(saved);

        return convertToDto(saved);
    }

    /**
     * Синхронизирует автоматическую запись-приход в текущем счёте
     * для статьи "Текущий счёт" карт-счёта.
     */
    private void syncCurrentAccountIncome(FinanceRecord source) {
        FinanceRecord linked = recordRepository.findFirstBySourceRecordId(source.getId()).orElse(null);

        if (source.getFinanceType() != FinanceType.NON_CASH) {
            if (linked != null) {
                recordRepository.delete(linked);
            }
            return;
        }

        BigDecimal amount = source.getExpCurrentAccount();

        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            if (linked == null) {
                linked = new FinanceRecord();
                linked.setSourceRecordId(source.getId());
                linked.setFinanceType(FinanceType.CURRENT_ACCOUNT);
            }
            linked.setRecordDate(source.getRecordDate());
            linked.setIncome(amount);
            linked.setExpUtilities(null);
            linked.setExpRent(null);
            linked.setExpMaterials(null);
            linked.setExpCredit(null);
            linked.setExpSalary(null);
            linked.setExpTaxes(null);
            linked.setExpAdvertising(null);
            linked.setExpPartners(null);
            linked.setExpOther(null);
            linked.setExpCurrentAccount(null);
            recordRepository.save(linked);
        } else if (linked != null) {
            recordRepository.delete(linked);
        }
    }

    @Transactional
    public void deleteRecord(Long id) {
        FinanceRecord record = recordRepository.findById(id).orElse(null);

        // Удаляем связанный автоматический приход, если удаляют исходную запись карт-счёта
        recordRepository.deleteBySourceRecordId(id);

        // Если удаляют автоматический приход напрямую — очищаем статью в исходной записи
        if (record != null && record.getSourceRecordId() != null) {
            recordRepository.findById(record.getSourceRecordId()).ifPresent(source -> {
                source.setExpCurrentAccount(null);
                recordRepository.save(source);
            });
        }

        recordRepository.deleteById(id);
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
        dto.setExpAdvertising(entity.getExpAdvertising());
        dto.setExpPartners(entity.getExpPartners());
        dto.setExpOther(entity.getExpOther());
        dto.setExpCurrentAccount(entity.getExpCurrentAccount());
        dto.setSourceRecordId(entity.getSourceRecordId());
        dto.setFinanceType(entity.getFinanceType());
        return dto;
    }
}