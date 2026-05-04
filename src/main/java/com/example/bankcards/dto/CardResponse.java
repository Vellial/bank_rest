package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CardResponse {
    private UUID id;
    private String maskedNumber;
    private String cardHolder;
    private String expiryDate;
    private CardStatus status;
    private BigDecimal balance;
}
