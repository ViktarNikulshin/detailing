package com.nikulshin.detailing.model.dto;

import com.nikulshin.detailing.model.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private Long id;
    private String clientName;
    private String clientPhone;
    private String carBrand;
    private String carModel;
    private String vin;
    private List<Long> workTypeIds;
    private List<Long> masterIds;
    private LocalDateTime executionDate;
    private OrderStatus status;
}
