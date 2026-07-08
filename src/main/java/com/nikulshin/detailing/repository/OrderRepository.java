package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.Order;
import com.nikulshin.detailing.model.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByArrivalDateBetween(LocalDateTime start, LocalDateTime end);
    List<Order> findByStatusAndDeliveryDateBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);
}
