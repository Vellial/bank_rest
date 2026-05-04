package com.example.bankcards.dto;

import java.util.Map;

public record ErrorResponse(
        String message,
        Map<String, String> errors
) {}
