package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.dto.CarBrandDto;
import com.nikulshin.detailing.model.dto.CarModelDto;
import com.nikulshin.detailing.model.dto.integration.AddCar;
import com.nikulshin.detailing.service.CarCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/car-brands")
    public ResponseEntity<String> addBrand(@RequestBody List<AddCar> brands) {
         service.addBrands(brands);
         return ResponseEntity.ok().build();
    }

    @PostMapping("/car-brands/{id}")
    public ResponseEntity<String> addModels(@PathVariable Long id, @RequestBody List<AddCar> models) {

        service.addModels(id, models);
        return ResponseEntity.ok().build();
    }
}

