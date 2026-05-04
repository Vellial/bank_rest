package com.example.bankcards.exception;

import java.util.UUID;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(UUID id) {
        super(String.format("Карта с ID %s не найдена", id));
    }
}