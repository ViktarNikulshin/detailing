package com.nikulshin.detailing.service;

import com.nikulshin.detailing.mapper.OrderMapper;
import com.nikulshin.detailing.model.domain.Dictionary;
import com.nikulshin.detailing.model.domain.Order;
import com.nikulshin.detailing.model.domain.OrderStatus;
import com.nikulshin.detailing.model.domain.User;
import com.nikulshin.detailing.model.dto.OrderDto;
import com.nikulshin.detailing.repository.DictionaryRepository;
import com.nikulshin.detailing.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final DictionaryRepository dictionaryRepository;
    private final OrderMapper orderMapper;
    private final UserService userService;

    public Order createOrder(OrderDto orderDto) {
        Order order = orderMapper.dtoToDomain(orderDto);
        if (orderDto.getWorkTypeIds() != null && !orderDto.getWorkTypeIds().isEmpty()) {
            List<Dictionary> workTypes = dictionaryRepository.findAllById(orderDto.getWorkTypeIds());
            order.setWorkTypes(workTypes);
        } else {
            order.setWorkTypes(new ArrayList<>());
        }


        if (orderDto.getMasterIds() != null && !orderDto.getMasterIds().isEmpty()) {
            List<User> masters = userService.getUsersByIds(orderDto.getMasterIds());
            order.setMasters(masters);
        }
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        return orderRepository.save(order);
    }

    public List<OrderDto> getOrdersByDateRange(LocalDateTime start, LocalDateTime end, Long masterId, String status) {
        List<Order> orders = orderRepository.findByExecutionDateBetween(start, end);
        if (masterId != null) {
            orders = orders
                    .stream()
                    .filter(o -> o.getMasters()
                            .stream()
                            .map(User::getId)
                            .toList().contains(masterId))
                    .toList();
        }
        if (status != null) {
            orders = orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.valueOf(status))
                    .toList();
        }
        return orderMapper.domainsToDtos(orders);
    }

    public OrderDto getOrdersById(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        return orderMapper.domainToDto(order);
    }

    public Order updateOrder(Long id, OrderDto orderDto) {
        Order order = orderMapper.dtoToDomain(orderDto);
        order.setId(id);
        List<Dictionary> workTypes = dictionaryRepository.findAllById(orderDto.getWorkTypeIds());
        order.setWorkTypes(workTypes);
        return orderRepository.save(order);
    }
}
