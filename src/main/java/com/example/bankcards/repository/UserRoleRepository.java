package com.example.bankcards.repository;

import com.example.bankcards.entity.RoleType;
import com.example.bankcards.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    Optional<UserRole> findByRoleType(RoleType roleType);
}
