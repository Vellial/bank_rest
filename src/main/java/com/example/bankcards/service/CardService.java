package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardFilter;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.BankUser;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    public Page<CardResponse> getAll(CardFilter filter, Pageable pageable) {
        return null;
    }


    public CardResponse getById(UUID id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException(id));
        return toDto(card);
    }

    public CardResponse create(@Valid CardCreateRequest request) {

        return null;
    }

    public CardResponse update(UUID id, CardUpdateRequest request) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException(id));
        return null;
    }

    public void delete(UUID id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException(id));
        cardRepository.delete(card);
    }

    public TransferResponse transfer(TransferRequest request, String username) {
        return null;
    }


    public BigDecimal getBalance(UUID id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException(id));
        return card.getBalance();
    }

    @Transactional
    public void updateStatus(UUID id, CardStatus status) {
        BankUser currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Только админ может менять статус карты");
        }
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException(id));
        card.setStatus(status);
    }

    public CardResponse toDto(Card card) {
        return new CardResponse(
                card.getId(),
                card.getMaskedNumber(),
                card.getCardHolder(),
                card.getExpiryDate().format(DateTimeFormatter.ofPattern("MM/yyyy")),
                card.getStatus(),
                card.getBalance()
        );
    }

}
