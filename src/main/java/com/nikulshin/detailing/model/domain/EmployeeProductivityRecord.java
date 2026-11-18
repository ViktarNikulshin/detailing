package com.nikulshin.detailing.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Запись продуктивности сотрудника:
 * чек, УСН, ЗП, налоги, аренда, материалы и итоговая прибыль.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "employee_productivity_record")
@Data
public class EmployeeProductivityRecord extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_date", nullable = false)
    private LocalDate date;

    // Услуга из справочника типов работ (денормализованное имя для истории)
    @Column(name = "work_type_id")
    private Long workTypeId;

    @Column(name = "work_type_name")
    private String workTypeName;

    // Связь к сотруднику (User)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    // Чек (сумма вручную)
    @Column(name = "check_amount", precision = 12, scale = 2)
    private BigDecimal checkAmount;

    // УСН = 6% от чека
    @Column(name = "usn_amount", precision = 12, scale = 2)
    private BigDecimal usnAmount;

    // З/П (сумма вручную)
    @Column(name = "salary", precision = 12, scale = 2)
    private BigDecimal salary;

    // Налоги = 58% от З/П
    @Column(name = "taxes_amount", precision = 12, scale = 2)
    private BigDecimal taxesAmount;

    // Аренда/место (сумма вручную)
    @Column(name = "rent_amount", precision = 12, scale = 2)
    private BigDecimal rentAmount;

    // Материалы (сумма вручную)
    @Column(name = "materials_amount", precision = 12, scale = 2)
    private BigDecimal materialsAmount;

    // Прибыль = чек - УСН - З/П - Налоги - Аренда - Материалы
    @Column(name = "profit", precision = 12, scale = 2)
    private BigDecimal profit;
}
