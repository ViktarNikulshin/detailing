package com.nikulshin.detailing.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "landlord_monthly_summary", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"year_value", "month_value"})
})
@Getter
@Setter
@NoArgsConstructor
public class LandlordMonthlySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_value", nullable = false)
    private int year;

    @Column(name = "month_value", nullable = false)
    private int month;

    @Column(name = "previous_balance", nullable = false)
    private BigDecimal previousBalance = BigDecimal.ZERO;

    @Column(name = "additional_agreement", nullable = false)
    private BigDecimal additionalAgreement = BigDecimal.ZERO;

    @OneToMany(mappedBy = "summary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LandlordServiceRecord> records = new ArrayList<>();

    public LandlordMonthlySummary(int year, int month, BigDecimal previousBalance, BigDecimal additionalAgreement) {
        this.year = year;
        this.month = month;
        this.previousBalance = previousBalance;
        this.additionalAgreement = additionalAgreement;
    }
}