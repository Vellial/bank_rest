package com.example.bankcards.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CardCreateRequest(
        @NotBlank(message = "Номер карты обязателен")
        String cardNumber,

        @NotBlank(message = "Владелец обязателен")
        @Size(min = 2, max = 100)
        String cardHolder,

        @NotNull(message = "Срок действия обязателен")
        @Future(message = "Срок действия должен быть в будущем")
        LocalDate expiryDate
) {}
