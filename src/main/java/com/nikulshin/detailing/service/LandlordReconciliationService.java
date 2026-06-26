package com.nikulshin.detailing.service;

import com.nikulshin.detailing.model.dto.report.*;
import com.nikulshin.detailing.model.domain.LandlordMonthlySummary;
import com.nikulshin.detailing.model.domain.LandlordServiceRecord;
import com.nikulshin.detailing.repository.LandlordRecordRepository;
import com.nikulshin.detailing.repository.LandlordSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LandlordReconciliationService {

    private final LandlordSummaryRepository summaryRepo;
    private final LandlordRecordRepository recordRepo;

    @Transactional(readOnly = true)
    public MonthlyReconciliationDto getMonthlySummary(int year, int month) {
        LandlordMonthlySummary summary = summaryRepo.findByYearAndMonth(year, month)
                .orElseGet(() -> new LandlordMonthlySummary(year, month, BigDecimal.ZERO, BigDecimal.ZERO));

        // Считаем сумму всех оказанных услуг в таблице
        BigDecimal totalServicesCost = summary.getRecords().stream()
                .map(LandlordServiceRecord::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Формула: Услуги + Остаток - Доп.соглашение
        BigDecimal currentBalance = totalServicesCost
                .add(summary.getPreviousBalance())
                .subtract(summary.getAdditionalAgreement());

        List<LandlordRecordDto> recordDtos = summary.getRecords().stream()
                .map(r -> new LandlordRecordDto(r.getId(), r.getDate(), r.getCarModel(), r.getWorkTypeName(), r.getCost()))
                .sorted(Comparator.comparing(LandlordRecordDto::date))
                .toList();

        return new MonthlyReconciliationDto(
                year, month,
                summary.getPreviousBalance(),
                summary.getAdditionalAgreement(),
                currentBalance,
                recordDtos
        );
    }

    @Transactional
    public void updateMonthlyMeta(UpdateMetaRequest request) {
        LandlordMonthlySummary summary = summaryRepo.findByYearAndMonth(request.year(), request.month())
                .orElseGet(() -> {
                    LandlordMonthlySummary s = new LandlordMonthlySummary();
                    s.setYear(request.year());
                    s.setMonth(request.month());
                    return s;
                });

        summary.setPreviousBalance(request.previousBalance());
        summary.setAdditionalAgreement(request.additionalAgreement());
        summaryRepo.save(summary);
    }

    @Transactional
    public void saveOrUpdateRecord(SaveRecordRequest request) {
        // Убедимся, что родительский месяц существует
        LandlordMonthlySummary summary = summaryRepo.findByYearAndMonth(request.year(), request.month())
                .orElseGet(() -> summaryRepo.save(new LandlordMonthlySummary(
                        request.year(), request.month(), BigDecimal.ZERO, BigDecimal.ZERO
                )));

        LandlordServiceRecord record;
        if (request.id() != null) {
            // Режим обновления существующей записи
            record = recordRepo.findById(request.id())
                    .orElseThrow(() -> new IllegalArgumentException("Запись не найдена с id: " + request.id()));
        } else {
            // Режим создания новой записи
            record = new LandlordServiceRecord();
            record.setSummary(summary);
        }

        record.setDate(request.date());
        record.setCarModel(request.carModel().trim().toUpperCase());
        record.setWorkTypeName(request.workTypeName().trim().toUpperCase());
        record.setCost(request.cost());

        recordRepo.save(record);
    }

    @Transactional
    public void deleteRecord(Long id) {
        if (!recordRepo.existsById(id)) {
            throw new IllegalArgumentException("Запись не найдена с id: " + id);
        }
        recordRepo.deleteById(id);
    }
}