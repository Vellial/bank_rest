package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardUpdateRequest(
        String cardNumber,
        LocalDate expiryDate,
        BigDecimal balance,
        CardStatus status
) {
}
