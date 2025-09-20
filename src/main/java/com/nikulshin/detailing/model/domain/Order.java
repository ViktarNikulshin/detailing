package com.nikulshin.detailing.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToMany
    @JoinTable(
            name = "order_work_types",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "dictionary_id")
    )
    private List<Dictionary> workTypes = new ArrayList<>();


    @ManyToMany
    @JoinTable(
            name = "order_user_master",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> masters;

    @Column(nullable = false)
    private LocalDateTime executionDate;

    private String beforePhotoPath;
    private String afterPhotoPath;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt;
}


