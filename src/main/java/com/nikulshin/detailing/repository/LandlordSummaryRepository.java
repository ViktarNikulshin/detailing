package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.LandlordMonthlySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LandlordSummaryRepository extends JpaRepository<LandlordMonthlySummary, Long> {
    Optional<LandlordMonthlySummary> findByYearAndMonth(int year, int month);
}