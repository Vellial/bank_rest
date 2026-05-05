package com.example.bankcards.dto.card;

import com.example.bankcards.entity.CardStatus;

import java.util.UUID;

public record CardFilter(
        CardStatus status,
        UUID userId
) {}
