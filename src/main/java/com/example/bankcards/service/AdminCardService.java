package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardBlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminCardService {

    private final CardBlockRequestRepository cardBlockRequestRepository;
    private final CardRepository cardRepository;

    @Transactional
    public void approveBlockRequest(UUID requestId) {
        CardBlockRequest request = cardBlockRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Запрос не найден"));

        if (request.getStatus() != CardBlockRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Запрос уже обработан");
        }

        request.setStatus(CardBlockRequest.RequestStatus.APPROVED);
        cardBlockRequestRepository.save(request);

        // Блокируем карту
        Card card = request.getCard();
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    public void rejectBlockRequest(UUID requestId, String rejectionReason) {
        CardBlockRequest request = cardBlockRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Запрос не найден"));

        if (request.getStatus() != CardBlockRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Запрос уже обработан");
        }

        request.setStatus(CardBlockRequest.RequestStatus.REJECTED);
        request.setReason(request.getReason() + " | Отклонено: " + rejectionReason);
        cardBlockRequestRepository.save(request);
    }
}
