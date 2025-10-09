package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.Order;
import com.nikulshin.detailing.model.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByExecutionDateBetween(LocalDateTime start, LocalDateTime end);
}
