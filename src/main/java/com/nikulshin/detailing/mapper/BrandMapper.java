package com.nikulshin.detailing.mapper;

import com.nikulshin.detailing.model.domain.CarBrand;
import com.nikulshin.detailing.model.dto.CarBrandDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BrandMapper extends BaseMapper <CarBrand, CarBrandDto> {
}
