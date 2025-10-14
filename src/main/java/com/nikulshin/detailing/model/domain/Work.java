package com.nikulshin.detailing.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "works")
@Data
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связь обратно к заказу (многие-к-одному)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude // Исключаем из toString, чтобы избежать рекурсии
    private Order order;

    // Основной тип работы (одна запись из Dictionary)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_type_dictionary_id", nullable = false)
    private Dictionary workType;

    // Комментарий к работе
    @Column(name = "comment")
    private String comment;

    // Дополнительные материалы/типы для этой работы (многие-ко-многим)
    @ManyToMany
    @JoinTable(
            name = "work_parts", // Новая связующая таблица
            joinColumns = @JoinColumn(name = "work_id"),
            inverseJoinColumns = @JoinColumn(name = "dictionary_id")
    )
    private List<Dictionary> parts = new ArrayList<>();
}