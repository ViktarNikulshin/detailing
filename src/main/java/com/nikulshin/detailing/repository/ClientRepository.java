package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByPhone(String phone);

    /**
     * Список клиентов с агрегатами по заказам (отменённые заказы не считаются визитом).
     * search — подстрока имени или телефона; пустая строка = без фильтра.
     */
    @Query("""
            SELECT c.id AS id, c.name AS name, c.phone AS phone, c.createdAt AS createdAt,
                   COUNT(o) AS ordersCount,
                   COALESCE(SUM(o.orderCost), 0) AS totalSpent,
                   MAX(o.arrivalDate) AS lastVisitDate
            FROM Client c
            LEFT JOIN Order o ON o.client = c
                AND o.status <> com.nikulshin.detailing.model.domain.OrderStatus.CANCELLED
            WHERE (:search = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR c.phone LIKE CONCAT('%', :search, '%'))
            GROUP BY c.id, c.name, c.phone, c.createdAt
            ORDER BY c.name
            """)
    List<ClientListRow> findClientsWithStats(@Param("search") String search);

    interface ClientListRow {
        Long getId();
        String getName();
        String getPhone();
        LocalDateTime getCreatedAt();
        Long getOrdersCount();
        Long getTotalSpent();
        LocalDateTime getLastVisitDate();
    }
}
