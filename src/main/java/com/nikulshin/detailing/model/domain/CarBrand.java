package com.nikulshin.detailing.model.domain;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "car_brands")
@Data
public class CarBrand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CarModel> models;

}
