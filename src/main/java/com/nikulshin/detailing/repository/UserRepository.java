package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    @Query(value = "select count (id) from users where role = ?1", nativeQuery = true)
    long countByRole(String role);
}