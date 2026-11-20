package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.PaymentCalendarSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentCalendarSettingsRepository extends JpaRepository<PaymentCalendarSettings, Long> {

    Optional<PaymentCalendarSettings> findTopByOrderByIdAsc();
}
