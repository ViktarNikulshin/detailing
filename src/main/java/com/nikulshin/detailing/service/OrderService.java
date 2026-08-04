package com.nikulshin.detailing.service;

import com.nikulshin.detailing.mapper.OrderMapper;
import com.nikulshin.detailing.model.domain.Client;
import com.nikulshin.detailing.model.domain.Order;
import com.nikulshin.detailing.model.domain.OrderStatus;
import com.nikulshin.detailing.model.dto.OrderDto;
import com.nikulshin.detailing.repository.ClientRepository;
import com.nikulshin.detailing.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.nikulshin.detailing.model.domain.OrderStatus.CANCELLED;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {
        Order order = orderMapper.dtoToDomain(orderDto);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);

        // Поиск или создание клиента
        Client client = findOrCreateClient(orderDto.getClientName(), orderDto.getClientPhone());
        order.setClient(client);

        if (order.getWorks() != null) {
            order.getWorks().forEach(work -> work.setOrder(order));
        }

        return orderMapper.domainToDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto updateOrder(Long id, OrderDto orderDto) {
        Order order = orderMapper.dtoToDomain(orderDto);
        order.setId(id);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(currentStatus(id));

        // Поиск или создание клиента при обновлении
        Client client = findOrCreateClient(orderDto.getClientName(), orderDto.getClientPhone());
        order.setClient(client);

        if (order.getWorks() != null) {
            order.getWorks().forEach(work -> work.setOrder(order));
        }

        return orderMapper.domainToDto(orderRepository.save(order));
    }

    private Client findOrCreateClient(String name, String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Номер телефона клиента обязателен");
        }

        // Нормализация телефона (опционально, для исключения дублей вида +7... и 8...)
        String cleanPhone = phone.trim();

        return clientRepository.findByPhone(cleanPhone)
                .map(existingClient -> {
                    // Если имя изменилось в заказе, обновляем его у клиента
                    if (name != null && !name.isBlank() && !name.equals(existingClient.getName())) {
                        existingClient.setName(name);
                        return clientRepository.save(existingClient);
                    }
                    return existingClient;
                })
                .orElseGet(() -> {
                    Client newClient = new Client();
                    newClient.setName(name);
                    newClient.setPhone(cleanPhone);
                    newClient.setCreatedAt(LocalDateTime.now());
                    return clientRepository.save(newClient);
                });
    }

    public List<OrderDto> getOrdersByDateRange(LocalDateTime start, LocalDateTime end, Long masterId, String status) {
        List<Order> orders = orderRepository.findByArrivalDateBetween(start, end);
        if (status != null) {
            return orderMapper.domainsToDtos(orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.valueOf(status))
                    .toList());
        }
        if (masterId != null) {
            return orderMapper.domainsToDtos(orders);
        }
        return orderMapper.domainsToDtos(orders
                .stream()
                .filter(o -> o.getStatus() != CANCELLED)
                .toList());
    }

    public OrderDto getOrdersById(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        return orderMapper.domainToDto(order);
    }

    private OrderStatus currentStatus(Long id) {
        return orderRepository.findById(id).orElseThrow(EntityNotFoundException::new).getStatus();
    }

    @Transactional
    public OrderDto changeStatus(Long id, String code, String masterId) {
        Order order = orderRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        order.setStatus(OrderStatus.valueOf(code));
        order.setCreatedAt(LocalDateTime.now());
        return orderMapper.domainToDto(orderRepository.save(order));
    }
}