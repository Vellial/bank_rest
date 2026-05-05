package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardBlockRequestDto;
import com.example.bankcards.dto.card.CardBlockResponse;
import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardFilter;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateRequest;

import com.example.bankcards.dto.transfer.TransferRequest;
import com.example.bankcards.dto.transfer.TransferResponse;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.AdminCardService;
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
import java.nio.file.AccessDeniedException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final AdminCardService adminCardService;

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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> create(@Valid @RequestBody CardCreateRequest request) {
        CardResponse created = cardService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public CardResponse update(@PathVariable UUID id,
                               @Valid @RequestBody CardUpdateRequest request) {
        return cardService.update(id, request);
    }

    @PostMapping("/{id}/request-block")
    public ResponseEntity<CardBlockResponse> requestCardBlock(
            @PathVariable UUID id,
            @RequestBody CardBlockRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        var response = cardService.requestCardBlock(id, userDetails.getUsername(), requestDto.reason());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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
    public BigDecimal getBalance(@PathVariable String cardNumber) {
        return cardService.getBalance(cardNumber);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponse updateStatus(
            @PathVariable UUID id,
            @RequestBody CardStatus status
    ) throws AccessDeniedException {
        return cardService.updateStatus(id, status);
    }

    @PutMapping("/admin/block-requests/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> approveBlockRequest(@PathVariable UUID id) {
        adminCardService.approveBlockRequest(id);
        return ResponseEntity.ok("Блокировка одобрена, карта заблокирована.");
    }

    @PutMapping("/admin/block-requests/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> rejectBlockRequest(@PathVariable UUID id, @RequestBody String reason) {
        adminCardService.rejectBlockRequest(id, reason);
        return ResponseEntity.ok("Запрос на блокировку отклонён.");
    }
}
