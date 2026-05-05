package com.example.bankcards.dto.card;

import jakarta.validation.constraints.NotBlank;

public record CardBlockRequestDto(
        @NotBlank(message = "Причина блокировки обязательна")
        String reason
) {}
