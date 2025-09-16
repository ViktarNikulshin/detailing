package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.domain.CarModel;
import com.nikulshin.detailing.model.dto.CarBrandDto;
import com.nikulshin.detailing.model.dto.CarModelDto;
import com.nikulshin.detailing.service.CarCatalogService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/car")
public class CarCatalogController {
    private final CarCatalogService service;

    public CarCatalogController(CarCatalogService service) {
        this.service = service;
    }

    @GetMapping("/car-brands")
    public List<CarBrandDto> getAllBrands() {
        return service.getAllBrands();
    }

    @GetMapping("/car-models/{brandId}")
    public List<CarModelDto> getModelsByBrand(@PathVariable Long brandId) {
        return service.getModelsByBrand(brandId);
    }
}

