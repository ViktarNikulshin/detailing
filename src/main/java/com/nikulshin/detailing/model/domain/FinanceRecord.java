package com.nikulshin.detailing.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "finance_records")
@Getter
@Setter
public class FinanceRecord extends Auditable { // Используем созданный ранее аудит!

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "income", precision = 12, scale = 2)
    private BigDecimal income; // Приход

    // Расходы разбиваем по графам из файла image_3c83c4.png
    @Column(name = "exp_utilities", precision = 12, scale = 2)
    private BigDecimal expUtilities; // Коммуналка

    @Column(name = "exp_rent", precision = 12, scale = 2)
    private BigDecimal expRent; // Аренда

    @Column(name = "exp_materials", precision = 12, scale = 2)
    private BigDecimal expMaterials; // Материалы

    @Column(name = "exp_credit", precision = 12, scale = 2)
    private BigDecimal expCredit; // Кредит

    @Column(name = "exp_salary", precision = 12, scale = 2)
    private BigDecimal expSalary; // З/п

    @Column(name = "exp_taxes", precision = 12, scale = 2)
    private BigDecimal expTaxes; // Налоги

    @Column(name = "exp_other", precision = 12, scale = 2)
    private BigDecimal expOther; // Прочее

    @Enumerated(EnumType.STRING)
    @Column(name = "finance_type", nullable = false)
    private FinanceType financeType;
}