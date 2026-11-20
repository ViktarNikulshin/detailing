package com.nikulshin.detailing.model.domain;

/**
 * Источник операции платёжного календаря.
 * ORDER_AUTO — создана автоматически из завершённого заказа (дедуплицируется по order_id).
 */
public enum PCSourceType {
    MANUAL,      // Ручная операция
    ORDER_AUTO   // Автоматически из завершённого заказа
}
