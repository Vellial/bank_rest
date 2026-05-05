package com.example.bankcards.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String user) {
        super(String.format("Пользователь %s не найден", user));
    }
}