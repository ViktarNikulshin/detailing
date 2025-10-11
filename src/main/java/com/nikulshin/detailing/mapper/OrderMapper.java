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

@Mapper(componentModel = "spring", uses = {DictionaryMapper.class,
        UserMapper.class, BrandMapper.class, ModelMapper.class})
public interface OrderMapper extends BaseMapper<Order, OrderDto> {


    @Mapping(target = "workTypes", ignore = true)
    @Mapping(target = "masters", ignore = true)
    Order dtoToDomain(OrderDto dto);

    @Mapping(target = "masterIds",  source = "masters", qualifiedByName = "extractIdsFromUser")
    OrderDto domainToDto(Order order);

    @Named("extractIdsFromDictionary")
    default List<Long> extractIdsFromDictionary(Collection<Dictionary> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(Dictionary::getId)
                .collect(Collectors.toList());
    }

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
