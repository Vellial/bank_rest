package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardBlockResponse;
import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardFilter;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateRequest;
import com.example.bankcards.dto.transfer.TransferRequest;
import com.example.bankcards.entity.BankUser;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.RequestStatus;
import com.example.bankcards.entity.RoleType;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.CardByIdNotFoundException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.BankUserRepository;
import com.example.bankcards.repository.CardBlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CardServiceTest {

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

    private final UUID userId = UUID.randomUUID();
    private final UUID cardId = UUID.randomUUID();
    private final String username = "testuser";
    private final String cardNumber = "4111111111111111";
    private final CardStatus status = CardStatus.ACTIVE;
    private final BigDecimal balance = new BigDecimal("1000.00");
    private final LocalDate expiryDate = LocalDate.now().plusYears(5);

    private Card createCard() {
        BankUser user = BankUser.builder()
                .id(userId)
                .username(username)
                .email("test@test.com")
                .build();

        return Card.builder()
                .id(cardId)
                .user(user)
                .cardNumber(cardNumber)
                .cardHolderName("TEST USER")
                .balance(balance)
                .status(status)
                .expiryDate(expiryDate)
                .build();
    }

    @Test
    void getAll_ReturnsMappedPage() {
        Card card = createCard();
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        when(cardRepository.findByUser(eq(userId), any(Pageable.class))).thenReturn(cardPage);

        Page<CardResponse> result = cardService.getAll(new CardFilter(CardStatus.ACTIVE, userId), Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().maskedNumber()).isEqualTo("**** **** **** 1111");
        verify(cardRepository).findByUser(eq(userId), any(Pageable.class));
    }

    @Test
    void getById_ValidId_ReturnsCardResponse() {
        Card card = createCard();
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        CardResponse response = cardService.getById(cardId);

        assertThat(response.id()).isEqualTo(cardId);
        assertThat(response.maskedNumber()).isEqualTo("**** **** **** 1111");
    }

    @Test
    void getById_InvalidId_ThrowsException() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardByIdNotFoundException.class, () -> cardService.getById(cardId));
    }

    @Test
    void create_ValidRequest_CreatesCard() {
        BankUser user = BankUser.builder().id(userId).username(username).build();
        when(securityUtils.getCurrentUser()).thenReturn(user);

        CardCreateRequest request = new CardCreateRequest(
                userId, "4222222222222222", expiryDate, new BigDecimal("500.00")
        );

        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardResponse response = cardService.create(request);

        assertThat(response.balance()).isEqualTo(new BigDecimal("500.00"));
        assertThat(response.maskedNumber()).isEqualTo("**** **** **** 2222");
        assertThat(response.cardHolder().getId()).isEqualTo(userId);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void update_ValidUpdate_OwnCard_Success() throws AccessDeniedException {
        BankUser user = BankUser.builder().id(userId).username(username).build();
        Card card = createCard();
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        CardUpdateRequest request = new CardUpdateRequest(
                null, null, new BigDecimal("800.00"), CardStatus.BLOCKED
        );

        CardResponse response = cardService.update(cardId, request);

        assertThat(response.status()).isEqualTo(CardStatus.BLOCKED);
        assertThat(response.balance()).isEqualTo(new BigDecimal("800.00"));
        verify(cardRepository).save(card);
    }

    @Test
    void update_UpdateOtherUserCard_ThrowsAccessDenied() throws AccessDeniedException {
        BankUser user = BankUser.builder().id(UUID.randomUUID()).username("other").build();
        Card card = createCard(); // принадлежит testuser
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThrows(AccessDeniedException.class, () ->
                cardService.update(cardId, new CardUpdateRequest(null, null, null, null))
        );
    }

    @Test
    void delete_ValidId_DeletesCard() {
        Card card = createCard();
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        cardService.delete(cardId);

        verify(cardRepository).delete(card);
    }

    @Test
    void delete_InvalidId_ThrowsException() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardByIdNotFoundException.class, () -> cardService.delete(cardId));
    }

    @Test
    void transfer_FromCardBlocked_ThrowsException() {
        Card fromCard = createCard();
        fromCard.setStatus(CardStatus.BLOCKED);
        Card toCard = createCard();
        toCard.setId(UUID.randomUUID());
        toCard.setCardNumber("4222222222222222");

        when(bankUserRepository.findByUsername(username)).thenReturn(Optional.of(BankUser.builder().id(userId).build()));
        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumber("4222222222222222")).thenReturn(Optional.of(toCard));

        TransferRequest request = new TransferRequest(cardNumber, "4222222222222222", new BigDecimal("100"));

        assertThrows(IllegalArgumentException.class, () -> cardService.transfer(request, username));
    }

    @Test
    void transfer_ToCardBlocked_ThrowsException() {
        Card fromCard = createCard();
        Card toCard = createCard();
        toCard.setId(UUID.randomUUID());
        toCard.setCardNumber("4222222222222222");
        toCard.setStatus(CardStatus.BLOCKED);

        when(bankUserRepository.findByUsername(username)).thenReturn(Optional.of(BankUser.builder().id(userId).build()));
        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumber("4222222222222222")).thenReturn(Optional.of(toCard));

        TransferRequest request = new TransferRequest(cardNumber, "4222222222222222", new BigDecimal("100"));

        assertThrows(IllegalArgumentException.class, () -> cardService.transfer(request, username));
    }

    @Test
    void transfer_ZeroOrNegativeAmount_ThrowsException() {
        Card fromCard = createCard();
        Card toCard = createCard();
        toCard.setId(UUID.randomUUID());
        toCard.setCardNumber("4222222222222222");

        when(bankUserRepository.findByUsername(username)).thenReturn(Optional.of(BankUser.builder().id(userId).build()));
        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumber("4222222222222222")).thenReturn(Optional.of(toCard));

        TransferRequest request = new TransferRequest(cardNumber, "4222222222222222", new BigDecimal("-100"));

        assertThrows(IllegalArgumentException.class, () -> cardService.transfer(request, username));
    }

    @Test
    void getBalance_ValidCard_ReturnsBalance() {
        Card card = createCard();
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        BigDecimal result = cardService.getBalance(cardId);

        assertThat(result).isEqualTo(balance);
    }

    @Test
    void getBalance_InvalidCard_ThrowsException() {
        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.empty());

        assertThrows(CardByIdNotFoundException.class, () -> cardService.getBalance(cardId));
    }

    @Test
    void updateStatus_Admin_CanUpdateStatus() throws AccessDeniedException {
        UserRole adminRole = new UserRole();
        adminRole.setRoleType(RoleType.ADMIN);
        BankUser admin = BankUser.builder().id(userId).userRole(adminRole).build();
        Card card = createCard();

        when(securityUtils.getCurrentUser()).thenReturn(admin);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        doReturn(card).when(cardRepository).save(any(Card.class));

        CardResponse response = cardService.updateStatus(cardId, CardStatus.BLOCKED);

        assertThat(response.status()).isEqualTo(CardStatus.BLOCKED);
    }

    @Test
    void updateStatus_NonAdmin_ThrowsAccessDenied() throws AccessDeniedException {
        UserRole userRole = new UserRole();
        userRole.setRoleType(RoleType.USER);

        BankUser user = BankUser.builder().id(userId).userRole(userRole).build();
        Card card = createCard();
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThrows(AccessDeniedException.class, () -> cardService.updateStatus(cardId, CardStatus.BLOCKED));
    }

    @Test
    void requestCardBlock_ValidRequest_Success() throws AccessDeniedException {
        Card card = createCard();
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardBlockRequestRepository.findByCardIdAndStatus(cardId, RequestStatus.PENDING))
                .thenReturn(List.of());

        CardBlockResponse response = cardService.requestCardBlock(cardId, username, "Lost card");

        assertThat(response.message()).isEqualTo("Запрос на блокировку карты отправлен. Ожидайте подтверждения от администратора.");
        verify(cardBlockRequestRepository).save(any(CardBlockRequest.class));
    }

    @Test
    void requestCardBlock_DuplicatePendingRequest_ThrowsException() throws AccessDeniedException {
        Card card = createCard();
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardBlockRequestRepository.findByCardIdAndStatus(cardId, RequestStatus.PENDING))
                .thenReturn(List.of(new CardBlockRequest()));

        assertThrows(IllegalStateException.class, () ->
                cardService.requestCardBlock(cardId, username, "Lost card")
        );
    }

    @Test
    void requestCardBlock_OtherUser_ThrowsAccessDenied() throws AccessDeniedException {
        Card card = createCard();
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThrows(AccessDeniedException.class, () ->
                cardService.requestCardBlock(cardId, "otheruser", "Lost card")
        );
    }

    @Test
    void getAllCards_ReturnsMappedPage() {
        Card card = createCard();
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        when(cardRepository.getAllCards(any(Pageable.class))).thenReturn(cardPage);

        Page<CardResponse> result = cardService.getAllCards(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().maskedNumber()).isEqualTo("**** **** **** 1111");
        verify(cardRepository).getAllCards(any(Pageable.class));
    }

    @Test
    void toDto_MasksCardNumberAndFormatsExpiry() {
        Card card = Card.builder()
                .id(cardId)
                .user(BankUser.builder().id(userId).build())
                .cardNumber("4111111111111111")
                .balance(balance)
                .status(status)
                .expiryDate(expiryDate)
                .build();

        CardResponse dto = cardService.toDto(card);

        assertThat(dto.maskedNumber()).isEqualTo("**** **** **** 1111");
        assertThat(dto.expiryDate()).isEqualTo(expiryDate.format(java.time.format.DateTimeFormatter.ofPattern("MM/yyyy")));
    }
}
