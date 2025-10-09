package com.nikulshin.detailing.service;

import com.nikulshin.detailing.mapper.BrandMapper;
import com.nikulshin.detailing.mapper.ModelMapper;
import com.nikulshin.detailing.model.domain.CarBrand;
import com.nikulshin.detailing.model.domain.CarModel;
import com.nikulshin.detailing.model.dto.CarBrandDto;
import com.nikulshin.detailing.model.dto.CarModelDto;
import com.nikulshin.detailing.model.dto.integration.AddCar;
import com.nikulshin.detailing.repository.CarBrandRepository;
import com.nikulshin.detailing.repository.CarModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarCatalogService {
    private final CarBrandRepository brandRepository;
    private final CarModelRepository modelRepository;
    private final BrandMapper brandMapper;
    private final ModelMapper modelMapper;

    public List<CarBrandDto> getAllBrands() {
        return brandMapper.domainsToDtos(brandRepository.findAll());
    }

    public List<CarModelDto> getModelsByBrand(Long brandId) {
        return modelMapper.domainsToDtos(modelRepository.findByBrandId(brandId));
    }

    public void addBrands(List<AddCar> brands ) {
        brands.forEach(b -> {
            CarBrand brand = new  CarBrand();
//            brand.setId(b.getId());
            brand.setName(b.getName());
            brand.setModels(new ArrayList<>());
            brandRepository.save(brand);
        });
    }

    public void addModels(Long id, List<AddCar> models) {
        models.forEach(model -> {
            CarModel carModel = new  CarModel();
            carModel.setName(model.getName());
            carModel.setBrand(brandRepository.getReferenceById(id));
            modelRepository.save(carModel);
        });

    }
}
