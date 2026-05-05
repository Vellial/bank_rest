package com.example.bankcards.dto.card;

import java.util.UUID;

public record CardBlockResponse(
        UUID requestId,
        String message
) {}
