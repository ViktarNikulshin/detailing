package com.nikulshin.detailing.repository;


import com.nikulshin.detailing.model.domain.Dictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DictionaryRepository extends JpaRepository<Dictionary, Long>, JpaSpecificationExecutor<Dictionary> {

    Optional<Dictionary> findByCodeAndType(String code, String type);

    List<Dictionary> findByType(String type);

    List<Dictionary> findByTypeAndIsActiveTrue(String type);

    List<Dictionary> findByIsActiveTrue();

    boolean existsByCodeAndType(String code, String type);

    boolean existsByCodeAndTypeAndIdNot(String code, String type, Long id);
}