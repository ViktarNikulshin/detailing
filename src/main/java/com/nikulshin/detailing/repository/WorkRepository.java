package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.Work;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkRepository extends JpaRepository<Work, Integer> {

}
