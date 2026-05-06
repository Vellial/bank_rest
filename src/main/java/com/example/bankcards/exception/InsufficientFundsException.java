package com.example.bankcards.exception;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class InsufficientFundsException extends RuntimeException {
    private final BigDecimal available;
    private final BigDecimal requested;

    public InsufficientFundsException(BigDecimal available, BigDecimal requested) {
        super(String.format("Недостаточно средств. Доступно: %s, запрошено: %s", available, requested));
        this.available = available;
        this.requested = requested;
    }
}
