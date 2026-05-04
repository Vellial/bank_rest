package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardFilter;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardUpdateRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.BankUser;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.RoleType;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final SecurityUtils securityUtils;

    public Page<CardResponse> getAll(CardFilter filter, Pageable pageable) {
        Page<Card> cards = cardRepository.findByUser(filter.userId(), pageable);

        return cards.map(this::toDto);
    }

    public CardResponse getById(UUID id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException(id));
        return toDto(card);
    }

    @Transactional
    public CardResponse create(@Valid CardCreateRequest request) {
        BankUser user = securityUtils.getCurrentUser();
        Card saved = cardRepository.save(Card.builder()
                .user(user)
                .balance(request.balance())
                .status(CardStatus.ACTIVE)
                .expiryDate(request.expiryDate())
                .cardNumber(request.cardNumber())
                .build());
        return toDto(saved);
    }

    @Transactional
    public CardResponse update(UUID id, CardUpdateRequest request) throws AccessDeniedException {
        BankUser user = securityUtils.getCurrentUser();
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException(id));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Карта не принадлежит пользователю");
        }

        if (request.expiryDate() != null) {
            card.setExpiryDate(request.expiryDate());
        }
        if (request.status() != null) {
            card.setStatus(request.status());
        }
        if (request.cardNumber() != null) {
            card.setCardNumber(request.cardNumber());
        }
        if (request.balance() != null) {
            card.setBalance(request.balance());
        }

        Card saved = cardRepository.save(card);
        return toDto(saved);
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
    public CardResponse updateStatus(UUID id, CardStatus status) throws AccessDeniedException {
        BankUser currentUser = securityUtils.getCurrentUser();
        if (currentUser.getUserRole().getRoleType() != RoleType.ADMIN) {
            throw new AccessDeniedException("Только админ может менять статус карты");
        }
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException(id));
        card.setStatus(status);
    }

    public CardResponse toDto(Card card) {
        return new CardResponse(
                card.getId(),
                card.getMaskedNumber(),
                card.getUser(),
                card.getExpiryDate().format(DateTimeFormatter.ofPattern("MM/yyyy")),
                card.getStatus(),
                card.getBalance()
        );
    }

}
