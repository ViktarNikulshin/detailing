package com.nikulshin.detailing.service;

import com.nikulshin.detailing.model.domain.ActRecord;
import com.nikulshin.detailing.repository.ActRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActRecordService {

    private final ActRecordRepository actRecordRepository;

    @Transactional(readOnly = true)
    public List<ActRecord> getActsSummary(int year, int month) {
        return actRecordRepository.findByYearAndMonth(year, month);
    }

    @Transactional
    public ActRecord saveOrUpdateAct(ActRecord actRecord) {
        // Метод save автоматически сделает update, если придет существующий id
        return actRecordRepository.save(actRecord);
    }

    @Transactional
    public void deleteAct(Long id) {
        if (!actRecordRepository.existsById(id)) {
            throw new IllegalArgumentException("Акт с ID " + id + " не найден");
        }
        actRecordRepository.deleteById(id);
    }
}