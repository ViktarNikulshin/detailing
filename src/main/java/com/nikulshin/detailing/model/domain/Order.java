package com.nikulshin.detailing.model.domain;

import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_brand_id")
    private CarBrand carBrand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_model_id")
    private CarModel carModel;

    @Column(unique = true)
    private String vin;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Work> works = new ArrayList<>();


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
    private Integer orderCost;
    public void addWork(Work work) {
        this.works.add(work);
        work.setOrder(this); // 👈 Устанавливает обратную ссылку
    }

    public void removeWork(Work work) {
        this.works.remove(work);
        work.setOrder(null);
    }
}