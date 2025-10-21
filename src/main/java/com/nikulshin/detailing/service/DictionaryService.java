package com.nikulshin.detailing.service;

import com.nikulshin.detailing.model.domain.Dictionary;
import com.nikulshin.detailing.model.dto.DictionaryDto;
import com.nikulshin.detailing.model.dto.DictionaryRequest;

import java.util.List;

public interface DictionaryService {
    DictionaryDto createDictionaryItem(DictionaryDto request);

    DictionaryDto updateDictionaryItem(Long id, DictionaryDto request);

    DictionaryDto getDictionaryItemById(Long id);

    List<DictionaryDto> getAllDictionaryItems();

    List<DictionaryDto> getDictionaryItemsByType(String type);

    List<DictionaryDto> getActiveDictionaryItemsByType(String type);

    void deleteDictionaryItem(Long id);

    Dictionary getDictionaryEntityById(Long id);

    Dictionary getDictionaryEntityByCodeAndType(String code, String type);

    boolean existsByCodeAndType(String code, String type);
}
