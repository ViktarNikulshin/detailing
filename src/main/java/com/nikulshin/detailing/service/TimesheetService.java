package com.nikulshin.detailing.service;

import com.nikulshin.detailing.model.domain.MasterSalaryBalance;
import com.nikulshin.detailing.model.domain.TimesheetRecord;
import com.nikulshin.detailing.model.domain.User;
import com.nikulshin.detailing.model.dto.report.TimesheetDayDto;
import com.nikulshin.detailing.model.dto.report.TimesheetSaveRequest;
import com.nikulshin.detailing.model.dto.report.UserTimesheetDto;
import com.nikulshin.detailing.repository.MasterSalaryBalanceRepository;
import com.nikulshin.detailing.repository.TimesheetRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimesheetService {

    private final TimesheetRecordRepository repository;
    private final MasterSalaryBalanceRepository masterSalaryBalanceRepository;
    private final UserService userService;

    // Получение записей за месяц
    @Transactional(readOnly = true)
    public UserTimesheetDto getTimesheet(Long masterId, String monthStr) {
        YearMonth yearMonth = YearMonth.parse(monthStr); // Парсим "YYYY-MM"
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        User user = userService.getById(masterId);
        BigDecimal previousBalance = masterSalaryBalanceRepository
                .findByMasterAndYearAndMonth(user, start.getYear(), start.getMonthValue())
                .map(MasterSalaryBalance::getPreviousBalance)
                .orElse(new BigDecimal(0));
        BigDecimal interimPayments = masterSalaryBalanceRepository
                .findByMasterAndYearAndMonth(user, start.getYear(), start.getMonthValue())
                .map(MasterSalaryBalance::getInterimPayments)
                .orElse(new BigDecimal(0));
        List<TimesheetRecord> records = repository.findByMasterIdAndRecordDateBetween(masterId, start, end);
        List<TimesheetDayDto> recordDto = records.stream()
                .map(r -> new TimesheetDayDto(r.getRecordDate(), r.isAbsent(), r.getContentRub(), r.getSalaryRub()))
                .toList();
        return new UserTimesheetDto(previousBalance, interimPayments, recordDto);

    }

    // Сохранение табеля за месяц (Upsert)
    @Transactional
    public void saveTimesheet(TimesheetSaveRequest request) {
        YearMonth yearMonth = YearMonth.parse(request.month());
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        // 1. Получаем то, что уже сохранено в базе за этот месяц
        List<TimesheetRecord> existingRecords = repository.findByMasterIdAndRecordDateBetween(request.masterId(), start, end);

        // Мапим по дате для быстрого поиска
        Map<LocalDate, TimesheetRecord> existingMap = existingRecords.stream()
                .collect(Collectors.toMap(TimesheetRecord::getRecordDate, Function.identity()));

        List<TimesheetRecord> toSave = new ArrayList<>();

        // 2. Мержим данные с фронтенда с данными из БД
        for (TimesheetDayDto dto : request.records()) {
            TimesheetRecord record = existingMap.get(dto.date());

            if (record == null) {
                // Если записи на этот день еще нет в БД — создаем новую
                record = new TimesheetRecord();
                record.setMasterId(request.masterId());
                record.setRecordDate(dto.date());
            }

            // Обновляем поля
            record.setAbsent(dto.isAbsent());
            // Если мастер отсутствовал, можно принудительно писать 0 или null на бэке для безопасности
            record.setContentRub(dto.isAbsent() ? null : dto.contentRub());
            record.setSalaryRub(dto.isAbsent() ? null : dto.salaryRub());

            toSave.add(record);
        }

        // 3. Сохраняем всё одним паком (Spring / Hibernate оптимизирует это под batch-запрос)
        repository.saveAll(toSave);
    }
}