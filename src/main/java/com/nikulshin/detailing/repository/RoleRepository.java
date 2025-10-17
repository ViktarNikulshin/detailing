package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Set<Role> findALLByIdIn(List<Long> ids);
}
