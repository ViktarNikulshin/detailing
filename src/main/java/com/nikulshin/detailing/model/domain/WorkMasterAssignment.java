package com.nikulshin.detailing.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "work_master_assignments")
@Data
public class WorkMasterAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связь к работе
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Work work;

    // Связь к мастеру (User)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "master_id", nullable = false)
    private User master;

    // Индивидуальный процент на ЗП для этого мастера по этой работе
    @Column(name = "salary_percent", nullable = false)
    private Double salaryPercent;
}