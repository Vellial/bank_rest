package com.example.bankcards.dto.card;

import com.example.bankcards.entity.BankUser;
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
    private BankUser cardHolder;
    private String cardHolderName;
    private String expiryDate;
    private CardStatus status;
    private BigDecimal balance;
}
