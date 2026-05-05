package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.RequestStatus;
import com.example.bankcards.repository.CardBlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCardServiceTest {

    @Mock
    private CardBlockRequestRepository cardBlockRequestRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private AdminCardService adminCardService;

    private UUID requestId;
    private CardBlockRequest pendingRequest;
    private Card card;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        card = new Card();
        card.setId(UUID.randomUUID());
        card.setStatus(CardStatus.ACTIVE);
        pendingRequest = new CardBlockRequest();
        pendingRequest.setId(requestId);
        pendingRequest.setCard(card);
        pendingRequest.setStatus(RequestStatus.PENDING);
        pendingRequest.setReason("Initial reason");
    }

    @Test
    void approveBlockRequest_shouldApproveRequestAndBlockCard() {
        when(cardBlockRequestRepository.findById(requestId)).thenReturn(Optional.of(pendingRequest));

        adminCardService.approveBlockRequest(requestId);

        assertEquals(RequestStatus.APPROVED, pendingRequest.getStatus());
        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardBlockRequestRepository).save(pendingRequest);
        verify(cardRepository).save(card);
    }

    @Test
    void approveBlockRequest_shouldThrowIllegalStateException_whenAlreadyProcessed() {
        pendingRequest.setStatus(RequestStatus.APPROVED);
        when(cardBlockRequestRepository.findById(requestId)).thenReturn(Optional.of(pendingRequest));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> adminCardService.approveBlockRequest(requestId));
        assertEquals("Запрос уже обработан", exception.getMessage());
    }

    @Test
    void approveBlockRequest_shouldThrowIllegalArgumentException_whenRequestNotFound() {
        when(cardBlockRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> adminCardService.approveBlockRequest(requestId));
        assertEquals("Запрос не найден", exception.getMessage());
    }

    @Test
    void rejectBlockRequest_shouldRejectRequestAndAppendReason() {
        String rejectionReason = "Некорректные данные";
        when(cardBlockRequestRepository.findById(requestId)).thenReturn(Optional.of(pendingRequest));

        adminCardService.rejectBlockRequest(requestId, rejectionReason);

        assertEquals(RequestStatus.REJECTED, pendingRequest.getStatus());
        assertEquals("Initial reason | Отклонено: " + rejectionReason, pendingRequest.getReason());
        verify(cardBlockRequestRepository).save(pendingRequest);
    }

    @Test
    void rejectBlockRequest_shouldThrowIllegalStateException_whenAlreadyProcessed() {
        pendingRequest.setStatus(RequestStatus.REJECTED);
        when(cardBlockRequestRepository.findById(requestId)).thenReturn(Optional.of(pendingRequest));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> adminCardService.rejectBlockRequest(requestId, "reason"));
        assertEquals("Запрос уже обработан", exception.getMessage());
    }

    @Test
    void rejectBlockRequest_shouldThrowIllegalArgumentException_whenRequestNotFound() {
        when(cardBlockRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> adminCardService.rejectBlockRequest(requestId, "reason"));
        assertEquals("Запрос не найден", exception.getMessage());
    }

    @Test
    void rejectBlockRequest_shouldHandleNullRejectionReason() {
        when(cardBlockRequestRepository.findById(requestId)).thenReturn(Optional.of(pendingRequest));

        adminCardService.rejectBlockRequest(requestId, null);

        assertEquals(RequestStatus.REJECTED, pendingRequest.getStatus());
        assertEquals("Initial reason | Отклонено: null", pendingRequest.getReason());
        verify(cardBlockRequestRepository).save(pendingRequest);
    }
}
