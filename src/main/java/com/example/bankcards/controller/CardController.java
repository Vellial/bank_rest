package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardFilter;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public Page<CardResponse> getAll(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            CardFilter filter
    ) {
        return cardService.getAll(filter, pageable);
    }

    @GetMapping("/{id}")
    public CardResponse getById(@PathVariable UUID id) {
        return cardService.getById(id);
    }

    @PostMapping
    public ResponseEntity<CardResponse> create(@Valid @RequestBody CardCreateRequest request) {
        CardResponse created = cardService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public CardResponse update(@PathVariable UUID id,
                               @Valid @RequestBody CardUpdateRequest request) {
        return cardService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        TransferResponse result = cardService.transfer(request, userDetails.getUsername());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/balance")
    public BigDecimal getBalance(@PathVariable UUID id) {
        return cardService.getBalance(id);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponse updateStatus(
            @PathVariable UUID id,
            @RequestBody CardStatus status
    ) {
        return cardService.updateStatus(id, status);
    }
}
