package com.example.bankcards.controller;

import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.user.UserUpdateRequest;
import com.example.bankcards.entity.BankUser;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.security.JWTTokenProvider;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@WithMockUser(roles = "ADMIN")
@AutoConfigureMockMvc(addFilters = false)
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private JWTTokenProvider jwtTokenProvider;

    @Test
    void getAllUsers_ShouldReturnOk() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse userResponse = new UserResponse(userId, "testuser", "Test User", "test@example.com");
        Page<UserResponse> page = new PageImpl<>(List.of(userResponse));
        when(userService.getAll(any())).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(userId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].username").value("testuser"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value("Test User"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].email").value("test@example.com"));

        verify(userService).getAll(any());
    }

    @Test
    void getUserById_ShouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponse userResponse = new UserResponse(id, "testuser", "Test User", "test@example.com");
        when(userService.getById(id)).thenReturn(userResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("testuser"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test User"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("test@example.com"));

        verify(userService).getById(id);
    }

    @Test
    void getAllCards_ShouldReturnOk() throws Exception {
        UUID cardId = UUID.randomUUID();
        BankUser cardHolder = new BankUser();
        CardResponse cardResponse = new CardResponse(
                cardId,
                "**** **** **** 1234",
                cardHolder,
                "John Doe",
                "12/25",
                CardStatus.ACTIVE,
                new BigDecimal("1500.00")
        );
        Page<CardResponse> page = new PageImpl<>(List.of(cardResponse));
        when(cardService.getAllCards(any())).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/users/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(cardId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].maskedNumber").value("**** **** **** 1234"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].cardHolderName").value("John Doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].expiryDate").value("12/25"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].balance").value(1500.00));

        verify(cardService).getAllCards(any());
    }

    @Test
    void blockUser_ShouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/admin/users/{id}/block", id))
                .andExpect(status().isOk());

        verify(userService).blockUser(id);
    }

    @Test
    void unblockUser_ShouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/admin/users/{id}/unblock", id))
                .andExpect(status().isOk());

        verify(userService).unblockUser(id);
    }

    @Test
    void updateUserRole_ShouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest(null, null, 1, null);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/admin/users/{id}/role", id)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("{\"roleId\":1}"))
                .andExpect(status().isOk());

        verify(userService).updateUser(id, request);
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/admin/users/{id}", id))
                .andExpect(status().is2xxSuccessful());

        verify(userService).deleteUser(id);
    }
}
