package com.example.bankcards.dto.transfer;

public record TransferResponse(
        boolean success,
        String message,
        String fromCardMasked,
        String toCardMasked
) {}