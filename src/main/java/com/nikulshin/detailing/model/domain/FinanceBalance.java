package com.nikulshin.detailing.model.domain;

import com.nikulshin.detailing.model.FinanceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "finance_balances", uniqueConstraints = {
        @UniqueConstraint(name = "uk_year_month", columnNames = {"year_period", "month_period", "finance_type"})
})
@Getter
@Setter
public class FinanceBalance extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_period", nullable = false)
    private Integer year;

    @Column(name = "month_period", nullable = false)
    private Integer month; // 1 - 12

    @Column(name = "starting_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal startingBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "finance_type", nullable = false)
    private FinanceType financeType;
}
