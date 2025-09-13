package com.nikulshin.detailing.service;

import com.nikulshin.detailing.model.domain.Order;
import com.nikulshin.detailing.model.domain.OrderStatus;
import com.nikulshin.detailing.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public Order createOrder(Order order) {
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByExecutionDateBetween(start, end);
    }
}
