package com.nikulshin.detailing.mapper;


import com.nikulshin.detailing.model.domain.Order;
import com.nikulshin.detailing.model.dto.OrderDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper extends BaseMapper<Order, OrderDto> {
}
