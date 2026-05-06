package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardBlockResponse;
import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardFilter;
import com.example.bankcards.dto.card.CardPageResponse;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateRequest;
import com.example.bankcards.dto.transfer.TransferRequest;
import com.example.bankcards.dto.transfer.TransferResponse;
import com.example.bankcards.entity.BankUser;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.CardByIdNotFoundException;
import com.example.bankcards.security.JWTTokenProvider;
import com.example.bankcards.service.AdminCardService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CardController.class)
public class CardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private AdminCardService adminCardService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JWTTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testCardId = UUID.randomUUID();

    private CardResponse createCardResponse() {
        UUID cardId = UUID.randomUUID();
        BankUser cardHolder = new BankUser();
        return new CardResponse(
                cardId,
                "**** **** **** 1234",
                cardHolder,
                "John Doe",
                "12/25",
                CardStatus.ACTIVE,
                new BigDecimal("1500.00")
        );
    }

    private CardResponse createCardResponse(UUID id) {
        BankUser cardHolder = new BankUser();
        return new CardResponse(
                id,
                "**** **** **** 1234",
                cardHolder,
                "John Doe",
                "12/25",
                CardStatus.ACTIVE,
                new BigDecimal("1500.00")
        );
    }

    @Test
    void getAllCards_ShouldReturnPageOfCards() throws Exception {
        CardResponse cardResponse = createCardResponse();

        Pageable pageable = PageRequest.of(0, 20);
        Page<CardResponse> page = new PageImpl<>(List.of(cardResponse), pageable, 1);

        when(cardService.getAll(any(CardFilter.class), any(Pageable.class))).thenReturn(page);

        MvcResult result = mockMvc.perform(get("/api/v1/cards")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0]").exists())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        CardPageResponse pageResponse = mapper.readValue(
                content,
                new TypeReference<CardPageResponse>() {}
        );
        List<CardResponse> responses = pageResponse.getContent();
        // Проверяем первый элемент
        assertCardResponse(responses.get(0), cardResponse);
    }

    private void assertCardResponse(CardResponse actual, CardResponse expected) {
        assertThat(actual.id()).isEqualTo(expected.id());
        assertThat(actual.maskedNumber()).isEqualTo(expected.maskedNumber());
        assertThat(actual.cardHolderName()).isEqualTo(expected.cardHolderName());
        assertThat(actual.expiryDate()).isEqualTo(expected.expiryDate());
        assertThat(actual.status()).isEqualTo(expected.status());
        assertThat(actual.balance()).isEqualTo(expected.balance());
    }

    @Test
    void getCardById_ShouldReturnCard() throws Exception {
        CardResponse cardResponse = createCardResponse(testCardId);

        when(cardService.getById(testCardId)).thenReturn(cardResponse);

        mockMvc.perform(get("/api/v1/cards/{id}", testCardId))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(testCardId.toString()));
    }

    @Test
    void createCard_ShouldReturnCreated() throws Exception {
        CardResponse cardResponse = createCardResponse();
        CardCreateRequest cardCreateRequest = createCardCreateRequest();

        when(cardService.create(any(CardCreateRequest.class))).thenReturn(cardResponse);

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    private CardCreateRequest createCardCreateRequest() {
        return new CardCreateRequest(UUID.randomUUID(), "2323423423445433", LocalDate.now().plusYears(5), BigDecimal.valueOf(1000));
    }

    @Test
    void updateCard_ShouldUpdateAndReturnCard() throws Exception {
        CardResponse cardResponse = createCardResponse(testCardId);
        CardUpdateRequest updateRequest = new CardUpdateRequest(cardResponse.maskedNumber(), cardResponse.expiryDate(), cardResponse.balance(), CardStatus.ACTIVE);
        when(cardService.update(eq(testCardId), any(CardUpdateRequest.class))).thenReturn(cardResponse);

        mockMvc.perform(put("/api/v1/cards/{id}", testCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCardId.toString()))
                .andExpect(jsonPath("$.maskedNumber").value(updateRequest.cardNumber()))
                .andExpect(jsonPath("$.expiryDate").value(updateRequest.expiryDate()))
                .andExpect(jsonPath("$.balance").value(1500.0))
                .andExpect(jsonPath("$.status").value(updateRequest.status().name()));;
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void requestCardBlock_ShouldReturnBlockResponse() throws Exception {
        UUID uuid = UUID.randomUUID();
        CardBlockResponse response = new CardBlockResponse(uuid, "Запрос на блокировку карты отправлен. Ожидайте подтверждения от администратора.");
        when(cardService.requestCardBlock(testCardId, "user@example.com", "Потеряна")).thenReturn(response);

        mockMvc.perform(post("/api/v1/cards/{id}/request-block", testCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Потеряна\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(uuid.toString()))
                .andExpect(jsonPath("$.message").value("Запрос на блокировку карты отправлен. Ожидайте подтверждения от администратора."));
    }

    @Test
    void deleteCard_ShouldReturnNoContent() throws Exception {
        doNothing().when(cardService).delete(testCardId);

        mockMvc.perform(delete("/api/v1/cards/{id}", testCardId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void transfer_ShouldReturnTransferResponse() throws Exception {
        TransferResponse response = new TransferResponse(true,
                "Перевод выполнен успешно",
                "**** **** **** 1234",
                "**** **** **** 5678");

        TransferRequest request = new TransferRequest(
                "1234 5678 9012 3456", "9876 5432 1098 7654", new BigDecimal("500.12"));
        when(cardService.transfer(any(TransferRequest.class), eq("user@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Перевод выполнен успешно"))
                .andExpect(jsonPath("$.fromCardMasked").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.toCardMasked").value("**** **** **** 5678"));
    }

    @Test
    void getBalance_ShouldReturnBalance() throws Exception {
        BigDecimal balance = new BigDecimal("1000.00");
        when(cardService.getBalance(testCardId)).thenReturn(balance);

        mockMvc.perform(get("/api/v1/cards/{id}/balance", testCardId))
                .andExpect(status().isOk())
                .andExpect(content().string(balance.toString()));
    }

    @Test
    void updateStatus_ShouldUpdateAndReturnCard() throws Exception {
        CardStatus status = CardStatus.ACTIVE;
        CardResponse cardResponse = createCardResponse(testCardId);
        when(cardService.updateStatus(testCardId, status)).thenReturn(cardResponse);

        mockMvc.perform(put("/api/v1/cards/{id}/status", testCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"ACTIVE\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCardId.toString()));
    }

    @Test
    void approveBlockRequest_ShouldReturnSuccessMessage() throws Exception {
        doNothing().when(adminCardService).approveBlockRequest(testCardId);

        mockMvc.perform(put("/api/v1/cards/admin/block-requests/{id}/approve", testCardId))
                .andExpect(status().isOk())
                .andExpect(content().string("Блокировка одобрена, карта заблокирована."));
    }

    @Test
    void rejectBlockRequest_ShouldReturnSuccessMessage() throws Exception {
        doNothing().when(adminCardService).rejectBlockRequest(testCardId, "Причина");

        mockMvc.perform(put("/api/v1/cards/admin/block-requests/{id}/reject", testCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"Причина\""))
                .andExpect(status().isOk())
                .andExpect(content().string("Запрос на блокировку отклонён."));
    }

    @Test
    void deleteCard_WithInvalidId_ShouldReturnNotFound() throws Exception {
        UUID invalidId = UUID.randomUUID();
        doThrow(new CardByIdNotFoundException(invalidId)).when(cardService).delete(invalidId);

        mockMvc.perform(delete("/api/v1/cards/{id}", invalidId))
                .andExpect(status().isNotFound());
    }
}
