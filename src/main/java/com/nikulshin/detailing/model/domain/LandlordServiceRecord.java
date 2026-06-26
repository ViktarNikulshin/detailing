package com.nikulshin.detailing.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "landlord_service_record")
@Getter
@Setter
@NoArgsConstructor
public class LandlordServiceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "car_model", nullable = false)
    private String carModel;

    @Column(name = "work_type_name", nullable = false)
    private String workTypeName;

    @Column(nullable = false)
    private BigDecimal cost = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summary_id", nullable = false)
    private LandlordMonthlySummary summary;
}