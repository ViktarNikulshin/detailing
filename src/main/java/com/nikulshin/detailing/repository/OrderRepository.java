package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.Order;
import com.nikulshin.detailing.model.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByArrivalDateBetween(LocalDateTime start, LocalDateTime end);
    List<Order> findByStatusAndDeliveryDateBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);
    List<Order> findByClientIdOrderByArrivalDateDesc(Long clientId);

    /**
     * Проекция для марки последнего автомобиля каждого клиента
     * (отменённые заказы исключены, сортировка от новых к старым).
     */
    @Query("""
            SELECT o.client.id AS clientId, o.carBrand AS carBrand
            FROM Order o
            WHERE o.status <> com.nikulshin.detailing.model.domain.OrderStatus.CANCELLED
            ORDER BY o.arrivalDate DESC
            """)
    List<ClientLastCarRow> findLastCarPerClient();

    interface ClientLastCarRow {
        Long getClientId();
        String getCarBrand();
    }
}
