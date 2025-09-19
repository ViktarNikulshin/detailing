package com.nikulshin.detailing.mapper;


import com.nikulshin.detailing.model.domain.Order;
import com.nikulshin.detailing.model.dto.OrderDto;
import com.nikulshin.detailing.model.dto.OrderResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",  uses = {DictionaryMapper.class})
public interface OrderMapper extends BaseMapper<Order, OrderDto> {


    @Mapping(target = "workTypes", ignore = true)
    @Mapping(target = "master", ignore = true)
    Order dtoToDomain(OrderDto dto);

    OrderDto domainToDto(Order order);
}
