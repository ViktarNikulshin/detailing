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

        Dictionary dictionary = dictionaryRequestMapper.dtoToDomain(request);
        Dictionary savedDictionary = dictionaryRepository.save(dictionary);

        log.info("Created dictionary item: {}", savedDictionary);
        return dictionaryMapper.domainToDto(savedDictionary);
    }

    @Override
    @Transactional
    public DictionaryDto updateDictionaryItem(Long id, DictionaryDto request) {
        Dictionary dictionary = getDictionaryEntityById(id);
        validateDictionaryItem(request, id);

        dictionaryRequestMapper.updateEntityFromRequest(request, dictionary);
        if (request.getParts() != null && !request.getParts().isEmpty()) {
            dictionaryRepository.deleteAll(dictionaryRepository.findByType(request.getCode()));
            request.getParts().forEach(requestPart -> {
                Dictionary dictionaryPart = new Dictionary();
                dictionaryPart.setCode(requestPart.getCode());
                dictionaryPart.setName(requestPart.getName());
                dictionaryPart.setType(request.getCode());
                dictionaryPart.setCreatedAt(LocalDateTime.now());
                dictionaryPart.setUpdatedAt(LocalDateTime.now());
                dictionaryPart.setIsActive(true);
                dictionaryRepository.save(dictionaryPart);
            });
        } else {
            dictionaryRepository.deleteAll(dictionaryRepository.findByType(request.getCode()));
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
        List<Dictionary> list = dictionaryRepository.findByType(type);
        return setDictionaryParts(list);
    }

    private List<DictionaryDto> setDictionaryParts(List<Dictionary> list) {
        List<DictionaryDto> dtos = dictionaryMapper.domainsToDtos(list);
        dtos.forEach(dto -> {
            dto.setParts(new ArrayList<>());
            dictionaryRepository.findByType(dto.getCode()).forEach(dto2 -> {
                dto.getParts().add(dictionaryMapper.domainToDto(dto2));
            });
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
        dictionaryRepository.deleteAll(dictionaryRepository.findByType(dictionary.getCode()));
        dictionaryRepository.delete(dictionary);
        log.info("Deleted dictionary item with id: {}", id);
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