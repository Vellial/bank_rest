package com.example.bankcards.exception;

import java.util.UUID;

public class CardAccessDeniedException extends RuntimeException {
  public CardAccessDeniedException(UUID cardId) {
    super(String.format("Нет доступа к карте %s", cardId));
  }
}
