package com.nikulshin.detailing.service;

import com.nikulshin.detailing.mapper.BrandMapper;
import com.nikulshin.detailing.mapper.ModelMapper;
import com.nikulshin.detailing.model.dto.CarBrandDto;
import com.nikulshin.detailing.model.dto.CarModelDto;
import com.nikulshin.detailing.repository.CarBrandRepository;
import com.nikulshin.detailing.repository.CarModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarCatalogService {
    private final CarBrandRepository brandRepository;
    private final CarModelRepository modelRepository;
    private final BrandMapper brandMapper;
    private final ModelMapper modelMapper;

    public List<CarBrandDto> getAllBrands() {
        return brandMapper.domainsToDtos(brandRepository.findAll()).stream().toList();
    }

    public List<CarModelDto> getModelsByBrand(Long brandId) {
        return modelMapper.domainsToDtos(modelRepository.findByBrandId(brandId)).stream().toList();
    }
}
