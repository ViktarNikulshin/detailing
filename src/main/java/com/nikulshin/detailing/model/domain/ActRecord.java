package com.nikulshin.detailing.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "act_records")
@Data
public class ActRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_date", nullable = false)
    private LocalDate date;

    @Column(name = "client", nullable = false)
    private String client;

    @Column(name = "car_model")
    private String carModel;

    // Использование точных финансовых вычислений
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ActStatus status;

    @Column(name = "has_documents", nullable = false)
    private boolean hasDocuments;
}