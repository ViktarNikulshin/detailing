package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.domain.Order;
import com.nikulshin.detailing.model.dto.OrderDto;
import com.nikulshin.detailing.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Order> createOrder(@RequestBody OrderDto order) {
        return ResponseEntity.ok(orderService.createOrder(order));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody OrderDto order) {
        return ResponseEntity.ok(orderService.updateOrder(id, order));
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<OrderDto>> getCalendarOrders(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end,
            @RequestParam(name = "masterId", required = false) Long masterId,
            @RequestParam(name = "status", required = false) String status) {
        return ResponseEntity.ok(orderService.getOrdersByDateRange(start, end, masterId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrdersById(id));
    }

    @GetMapping("/change/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id, @RequestParam String code, @RequestParam String master) {
        return ResponseEntity.ok(orderService.changeStatus(id, code, master));
    }
}