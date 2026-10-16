package com.nikulshin.detailing.service;

import com.nikulshin.detailing.model.domain.DebtRecord;
import com.nikulshin.detailing.model.domain.DebtType;
import com.nikulshin.detailing.repository.DebtRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DebtRecordService {

    private final DebtRecordRepository debtRecordRepository;

    @Transactional(readOnly = true)
    public List<DebtRecord> getDebtsSummary(int year, int month, DebtType type) {
        return debtRecordRepository.findByYearAndMonthAndType(year, month, type)
                .stream()
                .sorted(Comparator.comparing(DebtRecord::getDate))
                .toList();
    }

    @Transactional
    public DebtRecord saveOrUpdateDebt(DebtRecord debtRecord) {
        if (debtRecord.getDebtType() == null) {
            debtRecord.setDebtType(DebtType.CLIENT);
        }
        // Метод save автоматически сделает update, если придет существующий id
        return debtRecordRepository.save(debtRecord);
    }

    @Transactional
    public void deleteDebt(Long id) {
        if (!debtRecordRepository.existsById(id)) {
            throw new IllegalArgumentException("Акт с ID " + id + " не найден");
        }
        debtRecordRepository.deleteById(id);
    }
}