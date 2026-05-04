package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    @Query("SELECT c FROM Card c WHERE " +
            ":status IS NULL OR c.status = :status")
    Page<Card> findByFilters(CardStatus status, UUID userId, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId")
    Page<Card> findByUser(UUID userId, Pageable pageable);
}
