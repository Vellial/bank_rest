package com.example.bankcards.dto.user;

public record AuthResponse(
        String token,
        String refreshToken,
        String username,
        String role
) {
}
