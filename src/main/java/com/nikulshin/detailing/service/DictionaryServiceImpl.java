package com.nikulshin.detailing.service;

import com.nikulshin.detailing.mapper.DictionaryMapper;
import com.nikulshin.detailing.mapper.DictionaryRequestMapper;
import com.nikulshin.detailing.model.domain.Dictionary;
import com.nikulshin.detailing.model.dto.DictionaryDto;
import com.nikulshin.detailing.repository.DictionaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DictionaryServiceImpl implements DictionaryService {

    private final DictionaryRepository dictionaryRepository;
    private final DictionaryMapper dictionaryMapper;
    private final DictionaryRequestMapper dictionaryRequestMapper;

    @Override
    @Transactional
    public DictionaryDto createDictionaryItem(DictionaryDto request) {
        validateDictionaryItem(request, null);

        // Устанавливаем active = true по умолчанию для новых записей
        if (request.getActive() == null) {
            request.setActive(true);
        }

        Dictionary dictionary = dictionaryRequestMapper.dtoToDomain(request);
        Dictionary savedDictionary = dictionaryRepository.save(dictionary);

        log.info("Created dictionary item: {}", savedDictionary);
        return dictionaryMapper.domainToDto(savedDictionary);
    }

    @Override
    @Transactional
    public DictionaryDto updateDictionaryItem(Long id, DictionaryDto request) {
        if (request.getActive() == false) {
            deleteDictionaryItem(id);
        }
        Dictionary dictionary = getDictionaryEntityById(id);
        validateDictionaryItem(request, id);

        dictionaryRequestMapper.updateEntityFromRequest(request, dictionary);

        if (request.getParts() != null && !request.getParts().isEmpty()) {
            List<Dictionary> existingParts = dictionaryRepository.findByType(request.getCode());
            // Извлекаем коды существующих частей для быстрой проверки
            Set<String> existingPartCodes = existingParts.stream()
                    .map(Dictionary::getCode)
                    .collect(Collectors.toSet());

            // 1. Обработка частей из запроса (обновление ИЛИ создание)
            List<Dictionary> partsToSave = new ArrayList<>();
            Set<String> partCodesInRequest = request.getParts().stream()
                    .map(DictionaryDto::getCode)
                    .collect(Collectors.toSet());

            for (DictionaryDto partDto : request.getParts()) {
                Optional<Dictionary> existingPartOpt = existingParts.stream()
                        .filter(p -> p.getCode().equals(partDto.getCode()))
                        .findFirst();

                if (existingPartOpt.isPresent()) {
                    // ЛОГИКА 1: ОБНОВЛЕНИЕ СУЩЕСТВУЮЩЕЙ ЧАСТИ
                    Dictionary p = existingPartOpt.get();
                    if (!p.getIsActive().equals(partDto.getActive())) {
                        p.setIsActive(partDto.getActive());
                        partsToSave.add(p);
                    }
                } else {
                    // ЛОГИКА 2: СОЗДАНИЕ НОВОЙ ЧАСТИ (проверяем ОДИН раз для КАЖДОЙ partDto)
                    Dictionary dictionaryPart = new Dictionary();
                    dictionaryPart.setCode(partDto.getCode());
                    dictionaryPart.setName(partDto.getName());
                    dictionaryPart.setType(request.getCode());
                    dictionaryPart.setCreatedAt(LocalDateTime.now());
                    dictionaryPart.setUpdatedAt(LocalDateTime.now());
                    dictionaryPart.setIsActive(true); // Новые части всегда активны
                    partsToSave.add(dictionaryPart);
                    log.info("Creating new dictionary part: {}", dictionaryPart);
                }
            }

            // 3. ДЕАКТИВАЦИЯ ЧАСТЕЙ, ОТСУТСТВУЮЩИХ В ЗАПРОСЕ
            existingParts.stream()
                    .filter(p -> !partCodesInRequest.contains(p.getCode()))
                    .filter(Dictionary::getIsActive) // Деактивируем только активные
                    .forEach(p -> {
                        p.setIsActive(false);
                        partsToSave.add(p);
                    });

            if (!partsToSave.isEmpty()) {
                dictionaryRepository.saveAll(partsToSave);
                log.info("Saved/updated {} dictionary parts.", partsToSave.size());
            }

        } else {
            // Вместо удаления - деактивируем все
            List<Dictionary> partsToDeactivate = dictionaryRepository.findByType(request.getCode());
            partsToDeactivate.forEach(part -> part.setIsActive(false));
            dictionaryRepository.saveAll(partsToDeactivate);
            log.info("Deactivated {} parts for dictionary code: {}", partsToDeactivate.size(), request.getCode());
        }

        Dictionary updatedDictionary = dictionaryRepository.save(dictionary);

        log.info("Updated dictionary item: {}", updatedDictionary);
        return dictionaryMapper.domainToDto(updatedDictionary);
    }

    @Override
    @Transactional(readOnly = true)
    public DictionaryDto getDictionaryItemById(Long id) {
        Dictionary dictionary = getDictionaryEntityById(id);
        return dictionaryMapper.domainToDto(dictionary);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryDto> getAllDictionaryItems() {
        List<Dictionary> list = dictionaryRepository.findAll();
        return setDictionaryParts(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryDto> getDictionaryItemsByType(String type) {
        List<Dictionary> list = dictionaryRepository.findByTypeAndIsActiveTrue(type);
        return setDictionaryParts(list);
    }

    private List<DictionaryDto> setDictionaryParts(List<Dictionary> list) {
        List<DictionaryDto> dtos = dictionaryMapper.domainsToDtos(list);
        dtos.forEach(dto -> {
            List<Dictionary> activeParts = dictionaryRepository.findByType(dto.getCode());
            dto.setParts(dictionaryMapper.domainsToDtos(activeParts));
        });
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryDto> getActiveDictionaryItemsByType(String type) {
        return dictionaryMapper.domainsToDtos(dictionaryRepository.findByTypeAndIsActiveTrue(type));
    }

    @Override
    @Transactional
    public void deleteDictionaryItem(Long id) {
        Dictionary dictionary = getDictionaryEntityById(id);
        dictionary.setIsActive(false);
        List<Dictionary> partsToDeactivate = dictionaryRepository.findByType(dictionary.getCode());
        if (!partsToDeactivate.isEmpty()) {
            partsToDeactivate.forEach(part -> part.setIsActive(false));
            dictionaryRepository.saveAll(partsToDeactivate);
            log.info("Deactivated {} parts for dictionary id: {}", partsToDeactivate.size(), id);
        }

        dictionaryRepository.save(dictionary);
        log.info("Deactivated dictionary item with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Dictionary getDictionaryEntityById(Long id) {
        return dictionaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dictionary item not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Dictionary getDictionaryEntityByCodeAndType(String code, String type) {
        return dictionaryRepository.findByCodeAndType(code, type)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Dictionary item not found with code: " + code + " and type: " + type));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCodeAndType(String code, String type) {
        return dictionaryRepository.existsByCodeAndType(code, type);
    }

    private void validateDictionaryItem(DictionaryDto request, Long id) {
        boolean exists;
        if (id == null) {
            exists = dictionaryRepository.existsByCodeAndType(request.getCode(), request.getType());
        } else {
            exists = dictionaryRepository.existsByCodeAndTypeAndIdNot(
                    request.getCode(), request.getType(), id);
        }

        if (exists) {
            throw new IllegalArgumentException("Dictionary item with code '" + request.getCode() +
                    "' and type '" + request.getType() + "' already exists");
        }
    }
}