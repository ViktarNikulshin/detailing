package com.nikulshin.detailing.mapper;

import com.nikulshin.detailing.model.domain.CarModel;
import com.nikulshin.detailing.model.dto.CarModelDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ModelMapper extends BaseMapper<CarModel, CarModelDto> {
}
