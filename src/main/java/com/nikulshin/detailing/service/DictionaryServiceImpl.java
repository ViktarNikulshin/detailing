package com.nikulshin.detailing.service;

import com.nikulshin.detailing.mapper.DictionaryMapper;
import com.nikulshin.detailing.mapper.DictionaryRequestMapper;
import com.nikulshin.detailing.model.domain.Dictionary;
import com.nikulshin.detailing.model.dto.DictionaryDto;
import com.nikulshin.detailing.model.dto.DictionaryRequest;
import com.nikulshin.detailing.repository.DictionaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public DictionaryDto createDictionaryItem(DictionaryRequest request) {
        validateDictionaryItem(request, null);

        Dictionary dictionary = dictionaryRequestMapper.dtoToDomain(request);
        Dictionary savedDictionary = dictionaryRepository.save(dictionary);

        log.info("Created dictionary item: {}", savedDictionary);
        return dictionaryMapper.domainToDto(savedDictionary);
    }

    @Override
    @Transactional
    public DictionaryDto updateDictionaryItem(Long id, DictionaryRequest request) {
        Dictionary dictionary = getDictionaryEntityById(id);
        validateDictionaryItem(request, id);

        dictionaryRequestMapper.updateEntityFromRequest(request, dictionary);
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
        return dictionaryMapper.domainsToDtos(dictionaryRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryDto> getDictionaryItemsByType(String type) {
        return dictionaryMapper.domainsToDtos(dictionaryRepository.findByType(type));
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

    private void validateDictionaryItem(DictionaryRequest request, Long id) {
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