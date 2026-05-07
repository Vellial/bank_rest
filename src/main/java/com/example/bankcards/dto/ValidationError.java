package com.example.bankcards.dto;

public record ValidationError(
        String field,
        String message
) { }
