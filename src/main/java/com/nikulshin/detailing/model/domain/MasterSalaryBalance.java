package com.nikulshin.detailing.model.domain;

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

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "master_salary_balance")
@Data
public class MasterSalaryBalance extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Связь к мастеру (User)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "master_id", nullable = false)
    private User master;

    private Integer year;

    private Integer month;

    private BigDecimal previousBalance;

    private BigDecimal interimPayments;
}