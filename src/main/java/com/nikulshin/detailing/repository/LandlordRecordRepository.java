package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.LandlordServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LandlordRecordRepository extends JpaRepository<LandlordServiceRecord, Long> {
}