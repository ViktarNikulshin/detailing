package com.nikulshin.detailing.service;

import com.nikulshin.detailing.model.domain.Client;
import com.nikulshin.detailing.model.domain.Order;
import com.nikulshin.detailing.model.domain.OrderStatus;
import com.nikulshin.detailing.model.dto.ClientDetailsDto;
import com.nikulshin.detailing.model.dto.ClientDto;
import com.nikulshin.detailing.repository.ClientRepository;
import com.nikulshin.detailing.repository.ClientRepository.ClientListRow;
import com.nikulshin.detailing.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final OrderRepository orderRepository;

    /**
     * Клиентская база с агрегатами по заказам.
     * search — подстрока имени или телефона (пустая строка = все клиенты).
     */
    @Transactional(readOnly = true)
    public List<ClientDto> getClients(String search) {
        String normalized = search == null ? "" : search.trim();

        Map<Long, String> lastCarByClient = new HashMap<>();
        for (OrderRepository.ClientLastCarRow row : orderRepository.findLastCarPerClient()) {
            if (row.getClientId() != null && row.getCarBrand() != null) {
                lastCarByClient.putIfAbsent(row.getClientId(), row.getCarBrand());
            }
        }

        return clientRepository.findClientsWithStats(normalized).stream()
                .map(row -> toListDto(row, lastCarByClient.get(row.getId())))
                .toList();
    }

    /**
     * Карточка клиента: данные клиента, статистика и полная история заказов.
     */
    @Transactional(readOnly = true)
    public ClientDetailsDto getClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        List<Order> orders = orderRepository.findByClientIdOrderByArrivalDateDesc(id);

        List<ClientDetailsDto.ClientOrderDto> history = orders.stream()
                .map(order -> ClientDetailsDto.ClientOrderDto.builder()
                        .id(order.getId())
                        .carBrand(order.getCarBrand())
                        .status(order.getStatus() == null ? null : order.getStatus().name())
                        .orderCost(order.getOrderCost())
                        .arrivalDate(order.getArrivalDate())
                        .build())
                .toList();

        List<Order> visits = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .toList();

        long totalSpent = visits.stream()
                .map(order -> order.getOrderCost() == null ? 0 : order.getOrderCost())
                .mapToLong(Integer::longValue)
                .sum();

        LocalDateTime lastVisit = visits.stream()
                .map(Order::getArrivalDate)
                .filter(date -> date != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return ClientDetailsDto.builder()
                .id(client.getId())
                .name(client.getName())
                .phone(client.getPhone())
                .createdAt(client.getCreatedAt())
                .ordersCount((long) visits.size())
                .totalSpent(totalSpent)
                .lastVisitDate(lastVisit)
                .orders(history)
                .build();
    }

    /**
     * Поиск клиента по телефону — для подсказки в форме заказа.
     */
    @Transactional(readOnly = true)
    public Optional<ClientDetailsDto> findByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return Optional.empty();
        }
        return clientRepository.findByPhone(phone.trim())
                .map(client -> getClient(client.getId()));
    }

    private ClientDto toListDto(ClientListRow row, String lastCarBrand) {
        return ClientDto.builder()
                .id(row.getId())
                .name(row.getName())
                .phone(row.getPhone())
                .createdAt(row.getCreatedAt())
                .ordersCount(row.getOrdersCount() == null ? 0L : row.getOrdersCount())
                .totalSpent(row.getTotalSpent() == null ? 0L : row.getTotalSpent())
                .lastVisitDate(row.getLastVisitDate())
                .lastCarBrand(lastCarBrand)
                .build();
    }
}
