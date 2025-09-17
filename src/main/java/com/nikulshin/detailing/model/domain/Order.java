package com.nikulshin.detailing.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String clientName;

    @Column(nullable = false)
    private String clientPhone;

    @Column(nullable = false)
    private String carBrand;

    @Column(nullable = false)
    private String carModel;

    @Column(unique = true)
    private String vin;

    @Column(nullable = false)
    private String workType;

    @ManyToOne
    @JoinColumn(name = "master_id")
    private User master;

    @Column(nullable = false)
    private LocalDateTime executionDate;

    private String beforePhotoPath;
    private String afterPhotoPath;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt;
}

