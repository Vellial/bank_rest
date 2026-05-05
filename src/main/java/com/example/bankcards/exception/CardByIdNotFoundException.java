package com.example.bankcards.exception;

import java.util.UUID;

public class CardByIdNotFoundException extends RuntimeException {
    public CardByIdNotFoundException(UUID id) {
        super(String.format("Карта с ID %s не найдена", id));
    }
}