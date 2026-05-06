package com.example.bankcards.dto.card;

import com.example.bankcards.entity.CardStatus;

import java.math.BigDecimal;

public record CardUpdateRequest(
        String cardNumber,
        String expiryDate,
        BigDecimal balance,
        CardStatus status
) {
}
