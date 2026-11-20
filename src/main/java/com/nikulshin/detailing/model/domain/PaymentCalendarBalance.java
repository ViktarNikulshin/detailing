package com.nikulshin.detailing.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Стартовый (входящий) остаток платёжного календаря на месяц по счёту.
 * Если строки нет — остаток вычисляется переносом из предыдущих периодов
 * (см. PaymentCalendarService#resolveOpeningBalance).
 */
@Entity
@Table(name = "payment_calendar_balances", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pc_balance_year_month_account", columnNames = {"year_period", "month_period", "account_id"})
})
@Getter
@Setter
public class PaymentCalendarBalance extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_period", nullable = false)
    private Integer year;

    @Column(name = "month_period", nullable = false)
    private Integer month; // 1 - 12

    @Enumerated(EnumType.STRING)
    @Column(name = "account_id", nullable = false, length = 30)
    private FinanceType accountId;

    @Column(name = "starting_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal startingBalance;
}
