package com.nikulshin.detailing.service;

import com.nikulshin.detailing.model.domain.EmployeeProductivityRecord;
import com.nikulshin.detailing.model.domain.User;
import com.nikulshin.detailing.model.dto.report.EmployeeProductivityRecordDto;
import com.nikulshin.detailing.repository.EmployeeProductivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Сервис вкладки «Продуктивность сотрудника».
 * УСН     = 6%  от чека;
 * Налоги  = 58% от З/П;
 * Прибыль = чек - УСН - З/П - Налоги - Аренда/место - Материалы.
 */
@Service
@RequiredArgsConstructor
public class EmployeeProductivityService {

    private static final BigDecimal USN_RATE = new BigDecimal("0.06");
    private static final BigDecimal TAXES_RATE = new BigDecimal("0.58");

    private final EmployeeProductivityRepository repository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<EmployeeProductivityRecordDto> getRecords(Long employeeId, int year, int month) {
        User employee = userService.getById(employeeId);
        return repository.findByEmployeeAndYearAndMonth(employee, year, month)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public EmployeeProductivityRecordDto saveOrUpdate(EmployeeProductivityRecordDto dto) {
        if (dto.getEmployeeId() == null) {
            throw new IllegalArgumentException("Не указан сотрудник (employeeId)");
        }
        if (dto.getDate() == null) {
            throw new IllegalArgumentException("Не указана дата записи");
        }

        User employee = userService.getById(dto.getEmployeeId());
        // Если передан существующий id — обновляем, иначе создаем новую запись
        EmployeeProductivityRecord record = dto.getId() != null
                ? repository.findById(dto.getId()).orElseGet(EmployeeProductivityRecord::new)
                : new EmployeeProductivityRecord();

        record.setEmployee(employee);
        record.setDate(dto.getDate());
        record.setWorkTypeId(dto.getWorkTypeId());
        record.setWorkTypeName(dto.getWorkTypeName());
        record.setCheckAmount(nvl(dto.getCheckAmount()));
        record.setSalary(nvl(dto.getSalary()));
        record.setRentAmount(nvl(dto.getRentAmount()));
        record.setMaterialsAmount(nvl(dto.getMaterialsAmount()));

        // Расчетные колонки
        record.setUsnAmount(record.getCheckAmount().multiply(USN_RATE).setScale(2, RoundingMode.HALF_UP));
        record.setTaxesAmount(record.getSalary().multiply(TAXES_RATE).setScale(2, RoundingMode.HALF_UP));
        record.setProfit(record.getCheckAmount()
                .subtract(record.getUsnAmount())
                .subtract(record.getSalary())
                .subtract(record.getTaxesAmount())
                .subtract(record.getRentAmount())
                .subtract(record.getMaterialsAmount()));

        EmployeeProductivityRecord saved = repository.save(record);
        return toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Запись продуктивности с ID " + id + " не найдена");
        }
        repository.deleteById(id);
    }

    private EmployeeProductivityRecordDto toDto(EmployeeProductivityRecord record) {
        EmployeeProductivityRecordDto dto = new EmployeeProductivityRecordDto();
        dto.setId(record.getId());
        dto.setEmployeeId(record.getEmployee() != null ? record.getEmployee().getId() : null);
        dto.setDate(record.getDate());
        dto.setWorkTypeId(record.getWorkTypeId());
        dto.setWorkTypeName(record.getWorkTypeName());
        dto.setCheckAmount(record.getCheckAmount());
        dto.setUsnAmount(record.getUsnAmount());
        dto.setSalary(record.getSalary());
        dto.setTaxesAmount(record.getTaxesAmount());
        dto.setRentAmount(record.getRentAmount());
        dto.setMaterialsAmount(record.getMaterialsAmount());
        dto.setProfit(record.getProfit());
        return dto;
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
