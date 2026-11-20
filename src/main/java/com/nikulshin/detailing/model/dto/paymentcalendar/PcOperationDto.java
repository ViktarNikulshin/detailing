package com.nikulshin.detailing.model.dto.paymentcalendar;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Операция платёжного календаря (для отображения/редактирования). */
public record PcOperationDto(
        Long id,
        LocalDate date,
        String direction,        // INCOME / EXPENSE
        String category,         // PCCategory
        String account,          // FinanceType: CASH / NON_CASH / CURRENT_ACCOUNT
        BigDecimal amount,
        String description,
        String sourceType,       // MANUAL / ORDER_AUTO
        Long orderId,            // Для авто-операций: id заказа
        String orderClient,      // Клиент заказа (для авто-операций)
        String orderCar          // Автомобиль заказа (для авто-операций)
) {
}
