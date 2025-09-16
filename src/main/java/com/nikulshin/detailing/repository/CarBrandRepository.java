package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.CarBrand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarBrandRepository extends JpaRepository<CarBrand, Long> {
}
