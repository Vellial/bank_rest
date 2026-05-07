package com.example.bankcards.dto;

import java.util.List;

public record ErrorResponse(
        String message,
        List<ValidationError> errors
) {}
