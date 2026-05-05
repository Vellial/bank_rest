package com.example.bankcards.service;

import com.example.bankcards.dto.transfer.TransferRequest;
import com.example.bankcards.dto.transfer.TransferResponse;
import com.example.bankcards.entity.BankUser;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.BankUserRepository;
import com.example.bankcards.repository.CardBlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CardServiceTransferTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardBlockRequestRepository cardBlockRequestRepository;
    @Mock
    private BankUserRepository bankUserRepository;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private CardService cardService;

    private BankUser user;
    private Card fromCard;
    private Card toCard;

    @BeforeEach
    public void setUp() {
        user = new BankUser();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");

        fromCard = new Card();
        fromCard.setId(UUID.randomUUID());
        fromCard.setCardNumber("4111111111111111");
        fromCard.setUser(user);
        fromCard.setBalance(new BigDecimal("1000.00"));

        toCard = new Card();
        toCard.setId(UUID.randomUUID());
        toCard.setCardNumber("4222222222222222");
        toCard.setUser(new BankUser()); // другой пользователь
        toCard.setBalance(new BigDecimal("500.00"));

        when(bankUserRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cardRepository.findByCardNumber("4111111111111111")).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumber("4222222222222222")).thenReturn(Optional.of(toCard));
    }

    @Test
    public void transfer_Success() throws AccessDeniedException {
        TransferRequest request = new TransferRequest("4111111111111111", "4222222222222222", new BigDecimal("200.00"));

        TransferResponse response = cardService.transfer(request, "testuser");

        assertTrue(response.success());
        assertEquals("Перевод успешно выполнен", response.message());
        assertEquals("**** **** **** 1111", response.fromCardMasked());
        assertEquals("**** **** **** 2222", response.toCardMasked());
        assertEquals(new BigDecimal("800.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("700.00"), toCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    public void transfer_UserNotFound() {
        when(bankUserRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                cardService.transfer(new TransferRequest("4111111111111111", "4222222222222222", BigDecimal.ONE), "unknown")
        );
    }

    @Test
    public void transfer_FromCardNotFound() {
        when(cardRepository.findByCardNumber("invalid")).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () ->
                cardService.transfer(new TransferRequest("invalid", "4222222222222222", BigDecimal.ONE), "testuser")
        );
    }

    @Test
    public void transfer_ToCardNotFound() {
        when(cardRepository.findByCardNumber("4222222222222222")).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () ->
                cardService.transfer(new TransferRequest("4111111111111111", "invalid", BigDecimal.ONE), "testuser")
        );
    }

    @Test
    public void transfer_AccessDenied_NotOwner() {
        BankUser otherUser = new BankUser();
        otherUser.setId(UUID.randomUUID());
        otherUser.setUsername("otheruser");
        fromCard.setUser(otherUser);

        assertThrows(AccessDeniedException.class, () ->
                cardService.transfer(new TransferRequest("4111111111111111", "4222222222222222", BigDecimal.ONE), "testuser")
        );
    }

    @Test
    public void transfer_InsufficientFunds() {
        TransferRequest request = new TransferRequest("4111111111111111", "4222222222222222", new BigDecimal("1500.00"));

        assertThrows(InsufficientFundsException.class, () ->
                cardService.transfer(request, "testuser")
        );
    }

    @Test
    public void transfer_ZeroAmount() throws AccessDeniedException {
        TransferRequest request = new TransferRequest("4111111111111111", "4222222222222222", BigDecimal.ZERO);

        TransferResponse response = cardService.transfer(request, "testuser");

        assertTrue(response.success());
        assertEquals(new BigDecimal("1000.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("500.00"), toCard.getBalance());
    }

    @Test
    public void transfer_NegativeAmount() {
        TransferRequest request = new TransferRequest("4111111111111111", "4222222222222222", new BigDecimal("-100.00"));

        assertThrows(IllegalArgumentException.class, () ->
                cardService.transfer(request, "testuser")
        );
    }

}
