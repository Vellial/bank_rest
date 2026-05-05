package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId")
    Page<Card> findByUser(UUID userId, Pageable pageable);

    @Query("SELECT c FROM Card c")
    Page<Card> getAllCards(Pageable pageable);

    Optional<Card> findByCardNumber(@NotNull(message = "Номер карты отправки обязателен") String s);
}
