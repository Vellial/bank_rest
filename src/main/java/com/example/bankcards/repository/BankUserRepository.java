package com.example.bankcards.repository;

import com.example.bankcards.entity.BankUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BankUserRepository extends JpaRepository<BankUser, UUID> {

    Optional<BankUser> findByEmail(String email);

    Optional<BankUser> findByUsername(String username);
}
