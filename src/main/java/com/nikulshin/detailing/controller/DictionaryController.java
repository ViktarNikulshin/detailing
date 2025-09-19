package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.dto.DictionaryDto;
import com.nikulshin.detailing.model.dto.DictionaryRequest;
import com.nikulshin.detailing.service.DictionaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dictionary")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @PostMapping
    public ResponseEntity<DictionaryDto> createDictionaryItem(
            @Valid @RequestBody DictionaryRequest request) {
        DictionaryDto response = dictionaryService.createDictionaryItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DictionaryDto> updateDictionaryItem(
            @PathVariable Long id,
            @Valid @RequestBody DictionaryRequest request) {
        DictionaryDto response = dictionaryService.updateDictionaryItem(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DictionaryDto> getDictionaryItemById(@PathVariable Long id) {
        DictionaryDto response = dictionaryService.getDictionaryItemById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DictionaryDto>> getAllDictionaryItems() {
        List<DictionaryDto> responses = dictionaryService.getAllDictionaryItems();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<DictionaryDto>> getDictionaryItemsByType(
            @PathVariable String type) {
        List<DictionaryDto> responses = dictionaryService.getDictionaryItemsByType(type);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/type/{type}/active")
    public ResponseEntity<List<DictionaryDto>> getActiveDictionaryItemsByType(
            @PathVariable String type) {
        List<DictionaryDto> responses = dictionaryService.getActiveDictionaryItemsByType(type);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDictionaryItem(@PathVariable Long id) {
        dictionaryService.deleteDictionaryItem(id);
        return ResponseEntity.noContent().build();
    }
}