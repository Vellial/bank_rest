package com.example.bankcards.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class CardByIdNotFoundException extends RuntimeException {
    private final UUID id;

    public CardByIdNotFoundException(UUID id) {
        super(String.format("Карта с ID %s не найдена", id));
        this.id = id;
    }

}