package com.nikulshin.detailing.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "timesheet_records", uniqueConstraints = {
        @UniqueConstraint(name = "uk_master_date", columnNames = {"master_id", "record_date"})
})
@Getter
@Setter
public class TimesheetRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master_id", nullable = false)
    private Long masterId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "is_absent", nullable = false)
    private boolean isAbsent;

    // Используем BigDecimal для точных финансовых расчетов
    @Column(name = "content_rub", precision = 10, scale = 2)
    private BigDecimal contentRub;

    @Column(name = "salary_rub", precision = 10, scale = 2)
    private BigDecimal salaryRub;
}