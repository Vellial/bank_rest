package com.example.bankcards.dto.card;

import com.example.bankcards.entity.BankUser;
import com.example.bankcards.entity.CardStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CardResponse(
        UUID id,
        String maskedNumber,
        BankUser cardHolder,
        String cardHolderName,
        String expiryDate,
        CardStatus status,
        BigDecimal balance
) {}
