package com.example.ums.repository;

import com.example.ums.model.AppUser;
import com.example.ums.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
    List<AppUser> findByRole(Role role);
    Optional<AppUser> findByIdAndRole(Long id, Role role);
    long countByRole(Role role);
}
