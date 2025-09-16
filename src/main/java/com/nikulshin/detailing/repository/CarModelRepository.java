package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.CarModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CarModelRepository extends JpaRepository<CarModel, Long> {
    List<CarModel> findByBrandId(Long brandId);
}