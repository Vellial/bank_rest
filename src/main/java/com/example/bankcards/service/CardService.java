package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardBlockResponse;
import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardFilter;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateRequest;

import com.example.bankcards.dto.transfer.TransferRequest;
import com.example.bankcards.dto.transfer.TransferResponse;
import com.example.bankcards.entity.BankUser;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.RequestStatus;
import com.example.bankcards.entity.RoleType;
import com.example.bankcards.exception.CardByIdNotFoundException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.BankUserRepository;
import com.example.bankcards.repository.CardBlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardMaskingUtils;
import com.example.bankcards.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import org.springframework.security.access.AccessDeniedException;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CardBlockRequestRepository cardBlockRequestRepository;
    private final BankUserRepository bankUserRepository;
    private final SecurityUtils securityUtils;

    public Page<CardResponse> getAll(CardFilter filter, Pageable pageable) {
        Page<Card> cards = cardRepository.findByUser(filter.userId(), pageable);

        return cards.map(this::toDto);
    }

    public CardResponse getById(UUID id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardByIdNotFoundException(id));
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
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardByIdNotFoundException(id));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Карта не принадлежит пользователю");
        }

        if (request.expiryDate() != null) {
            YearMonth expiry = YearMonth.parse(request.expiryDate(),
                    DateTimeFormatter.ofPattern("MM/yy"));
            card.setExpiryDate(expiry.atDay(1));
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
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardByIdNotFoundException(id));
        cardRepository.delete(card);
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request, String username) throws AccessDeniedException {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть положительной");
        }

        BankUser user = bankUserRepository.findByUsername(username).orElseThrow(
                () -> new UserNotFoundException(username)
        );
        Card fromCard = cardRepository.findByCardNumber(request.fromCardId())
                .orElseThrow(() -> new CardNotFoundException(request.fromCardId()));

        if (!fromCard.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Карта не принадлежит пользователю");
        }
        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Карта отправителя заблокирована");
        }
        if (fromCard.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException(request.amount(), fromCard.getBalance());
        }
        Card toCard = cardRepository.findByCardNumber(request.toCardId())
                .orElseThrow(() -> new CardNotFoundException(request.toCardId()));
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Карта отправителя заблокирована");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(request.amount()));

        toCard.setBalance(toCard.getBalance().add(request.amount()));
        cardRepository.save(fromCard);
        cardRepository.save(toCard);
        return new TransferResponse(true,
                "Перевод успешно выполнен",
                CardMaskingUtils.maskCardNumber(fromCard.getCardNumber()),
                CardMaskingUtils.maskCardNumber(toCard.getCardNumber()));
    }

    public BigDecimal getBalance(UUID id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardByIdNotFoundException(id));
        return card.getBalance();
    }

    @Transactional
    public CardResponse updateStatus(UUID id, CardStatus status) throws AccessDeniedException {
        BankUser currentUser = securityUtils.getCurrentUser();
        if (currentUser.getUserRole().getRoleType() != RoleType.ADMIN) {
            throw new AccessDeniedException("Только админ может менять статус карты");
        }
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardByIdNotFoundException(id));
        card.setStatus(status);
        Card saved = cardRepository.save(card);
        return toDto(saved);
    }

    public CardBlockResponse requestCardBlock(UUID cardId, String username, String reason) throws AccessDeniedException {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardByIdNotFoundException(cardId));

        if (!card.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Карта не принадлежит пользователю");
        }

        if (!cardBlockRequestRepository.findByCardIdAndStatus(cardId, RequestStatus.PENDING).isEmpty()) {
            throw new IllegalStateException("Запрос на блокировку уже отправлен и ожидает обработки");
        }

        CardBlockRequest request = new CardBlockRequest();
        request.setCard(card);
        request.setUser(card.getUser());
        request.setReason(reason);

        cardBlockRequestRepository.save(request);

        return new CardBlockResponse(
                request.getId(),
                "Запрос на блокировку карты отправлен. Ожидайте подтверждения от администратора."
        );
    }

    public Page<CardResponse> getAllCards(Pageable pageable) {
        Page<Card> cards = cardRepository.getAllCards(pageable);

        return cards.map(this::toDto);
    }

    public CardResponse toDto(Card card) {
        return new CardResponse(
                card.getId(),
                CardMaskingUtils.maskCardNumber(card.getCardNumber()),
                card.getUser(),
                card.getCardHolderName(),
                card.getExpiryDate().format(DateTimeFormatter.ofPattern("MM/yyyy")),
                card.getStatus(),
                card.getBalance()
        );
    }
}
