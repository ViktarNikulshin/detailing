package com.nikulshin.detailing.model.dto.integration;

import lombok.Data;

import java.util.List;

@Data
public class AddBrands {
    List<AddCar> carModelList;
}
