package com.nikulshin.detailing.repository;

import com.nikulshin.detailing.model.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    @Query(value = "select count (id) from users where role = ?1", nativeQuery = true)
    long countByRole(String role);
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);
    @Query("SELECT u FROM User u JOIN FETCH u.roles r WHERE r.name = :code")
    List<User> findAllByRole(String code);
}