package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;

import java.time.LocalDate;

public record CardFilter(
        CardStatus status,
        String cardHolder,
        LocalDate dateFrom,
        LocalDate dateTo
) {}
