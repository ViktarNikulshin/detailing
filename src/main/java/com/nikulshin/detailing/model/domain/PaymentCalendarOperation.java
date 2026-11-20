package com.nikulshin.detailing.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Операция платёжного календаря (поступление или платёж).
 * Все данные персистируются на бэкенде; операции, автоматически созданные
 * из завершённых заказов, дедуплицируются уникальным ограничением
 * (source_type, order_id): для ORDER_AUTO операция на заказ одна,
 * для MANUAL order_id = NULL (в PostgreSQL NULL-значения в уникальном
 * индексе не конфликтуют, поэтому ручных операций может быть сколько угодно).
 */
@Entity
@Table(name = "payment_calendar_operations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pc_operation_source_order", columnNames = {"source_type", "order_id"})
})
@Getter
@Setter
public class PaymentCalendarOperation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_date", nullable = false)
    private LocalDate operationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 20)
    private PCDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private PCCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_id", nullable = false, length = 30)
    private FinanceType accountId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private PCSourceType sourceType;

    @Column(name = "order_id")
    private Long orderId;
}
