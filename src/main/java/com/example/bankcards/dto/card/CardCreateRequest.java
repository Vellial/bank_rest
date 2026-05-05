package com.example.bankcards.dto.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CardCreateRequest(
        @NotNull
        UUID userId,

        @NotBlank(message = "Номер карты обязателен")
        String cardNumber,

        @NotNull(message = "Срок действия обязателен")
        @Future(message = "Срок действия должен быть в будущем")
        LocalDate expiryDate,

        @NotNull
        BigDecimal balance
) {}
