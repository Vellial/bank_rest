package com.example.bankcards.dto.transfer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull
        String fromCardId,

        @NotNull
        String toCardId,

        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount
) {
}
