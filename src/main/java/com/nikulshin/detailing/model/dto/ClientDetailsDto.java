package com.nikulshin.detailing.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Карточка клиента: данные + агрегаты + полная история заказов.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDetailsDto {
    private Long id;
    private String name;
    private String phone;
    private LocalDateTime createdAt;
    private Long ordersCount;
    private Long totalSpent;
    private LocalDateTime lastVisitDate;
    private List<ClientOrderDto> orders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClientOrderDto {
        private Long id;
        private String carBrand;
        private String status;
        private Integer orderCost;
        private LocalDateTime arrivalDate;
    }
}
