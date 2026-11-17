package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.dto.ClientDetailsDto;
import com.nikulshin.detailing.model.dto.ClientDto;
import com.nikulshin.detailing.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    /**
     * Клиентская база с поиском по имени/телефону и агрегатами по заказам.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClientDto>> getClients(
            @RequestParam(name = "search", required = false) String search) {
        return ResponseEntity.ok(clientService.getClients(search));
    }

    /**
     * Карточка клиента с историей заказов.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientDetailsDto> getClient(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClient(id));
    }

    /**
     * Подсказка в форме заказа: поиск клиента по телефону.
     */
    @GetMapping("/by-phone")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ClientDetailsDto> getByPhone(
            @RequestParam(name = "phone") String phone) {
        return clientService.findByPhone(phone)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
