package com.example.bankcards.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(BigDecimal available, BigDecimal requested) {
        super(String.format("Недостаточно средств. Доступно: %s, запрошено: %s", available, requested));
    }
}
