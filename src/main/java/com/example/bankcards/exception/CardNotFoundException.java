package com.example.bankcards.exception;

import java.util.UUID;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(String number) {
        super(String.format("Карта с номером %s не найдена", number));
    }
}