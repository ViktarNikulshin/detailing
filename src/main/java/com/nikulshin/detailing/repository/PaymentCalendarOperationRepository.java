package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.FinanceType;
import com.nikulshin.detailing.model.domain.PCSourceType;
import com.nikulshin.detailing.model.domain.PaymentCalendarOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PaymentCalendarOperationRepository extends JpaRepository<PaymentCalendarOperation, Long> {

    List<PaymentCalendarOperation> findByOperationDateBetweenOrderByOperationDateAsc(LocalDate start, LocalDate end);

    List<PaymentCalendarOperation> findBySourceTypeAndOperationDateBetween(PCSourceType sourceType, LocalDate start, LocalDate end);

    Optional<PaymentCalendarOperation> findFirstBySourceTypeAndOrderId(PCSourceType sourceType, Long orderId);

    List<PaymentCalendarOperation> findBySourceTypeAndOrderIdIn(PCSourceType sourceType, Collection<Long> orderIds);

    /** Чистый поток (поступления минус платежи) по счёту до указанной даты (не включая её). */
    @Query("""
            SELECT COALESCE(SUM(CASE WHEN o.direction = com.nikulshin.detailing.model.domain.PCDirection.INCOME
                                     THEN o.amount ELSE -o.amount END), 0)
            FROM PaymentCalendarOperation o
            WHERE o.accountId = :account AND o.operationDate < :date
            """)
    BigDecimal netBefore(@Param("account") FinanceType account, @Param("date") LocalDate date);

    /** Чистый поток по счёту за диапазон [from, to). */
    @Query("""
            SELECT COALESCE(SUM(CASE WHEN o.direction = com.nikulshin.detailing.model.domain.PCDirection.INCOME
                                     THEN o.amount ELSE -o.amount END), 0)
            FROM PaymentCalendarOperation o
            WHERE o.accountId = :account AND o.operationDate >= :from AND o.operationDate < :to
            """)
    BigDecimal netBetween(@Param("account") FinanceType account,
                          @Param("from") LocalDate from,
                          @Param("to") LocalDate to);
}
