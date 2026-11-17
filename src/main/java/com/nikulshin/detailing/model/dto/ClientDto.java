package com.nikulshin.detailing.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Строка списка клиентской базы с агрегатами по заказам.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDto {
    private Long id;
    private String name;
    private String phone;
    private LocalDateTime createdAt;
    private Long ordersCount;
    private Long totalSpent;
    private LocalDateTime lastVisitDate;
    private String lastCarBrand;
}
