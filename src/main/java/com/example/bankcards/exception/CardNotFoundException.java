package com.example.bankcards.exception;

import lombok.Getter;

@Getter
public class CardNotFoundException extends RuntimeException {
    private final String number;

    public CardNotFoundException(String number) {
        super(String.format("Карта с номером %s не найдена", number));
        this.number=number;
    }
}