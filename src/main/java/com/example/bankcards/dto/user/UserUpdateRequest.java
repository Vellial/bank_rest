package com.example.bankcards.dto.user;

public record UserUpdateRequest(
        String email,
        String name,
        Integer roleId,
        Boolean blocked
) {
}
