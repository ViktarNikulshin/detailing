package com.nikulshin.detailing.mapper;


import com.nikulshin.detailing.model.domain.Dictionary;
import com.nikulshin.detailing.model.domain.Order;
import com.nikulshin.detailing.model.domain.User;
import com.nikulshin.detailing.model.dto.OrderDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {WorkMapper.class,
        UserMapper.class, BrandMapper.class, DictionaryMapper.class})
public interface OrderMapper extends BaseMapper<Order, OrderDto> {


    @Mapping(target = "masters", ignore = true)
    Order dtoToDomain(OrderDto dto);

    @Mapping(target = "masterIds",  source = "masters", qualifiedByName = "extractIdsFromUser")
    OrderDto domainToDto(Order order);

    @Named("extractIdsFromUser")
    default List<Long> extractIdsFromUser(Collection<User> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }
}
