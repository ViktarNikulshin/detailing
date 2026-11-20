package com.nikulshin.detailing.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Настройки платёжного календаря (singleton-строка, id = 1).
 */
@Entity
@Table(name = "payment_calendar_settings")
@Getter
@Setter
public class PaymentCalendarSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Счёт, на который по умолчанию зачисляются поступления
     * по автоматически подтянутым завершённым заказам.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "default_income_account", nullable = false, length = 30)
    private FinanceType defaultIncomeAccount = FinanceType.CASH;
}
