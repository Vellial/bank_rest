package com.example.bankcards.dto.card;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardPageResponse {
    private List<CardResponse> content;
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;
}