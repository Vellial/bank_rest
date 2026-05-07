package com.example.bankcards.controller;

import com.example.bankcards.dto.ErrorResponse;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Tag(name = "Card", description = "Работа с банковскими картами")
public class CardController {

    private final CardService cardService;
    private final AdminCardService adminCardService;

    @GetMapping
    @Operation(
        summary = "Все банковские карты", 
        description = "Возвращает список всех банковских карт",
        responses = {
            @ApiResponse(responseCode = "200", description = "Список карт успешно получен", content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public Page<CardResponse> getAll(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            CardFilter filter
    ) {
        return cardService.getAll(filter, pageable);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить карту по id", 
        description = "Возвращает карту, если она найдена",
        responses = {
            @ApiResponse(responseCode = "200", description = "Карта найдена", content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public CardResponse getById(@PathVariable UUID id) {
        return cardService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Создать банковскую карту в системе", 
        description = "Создаст банковскую карту",
        responses = {
            @ApiResponse(responseCode = "201", description = "Карта успешно создана", content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидные данные создания карты", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав на создание карты", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public ResponseEntity<CardResponse> create(@Valid @RequestBody CardCreateRequest request) {
        CardResponse created = cardService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Обновить данные банковской карты", 
        description = "Обновляет данные карты",
        responses = {
            @ApiResponse(responseCode = "200", description = "Данные карты обновлены", content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидные данные обновления", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав на обновление", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public CardResponse update(@PathVariable UUID id,
                               @Valid @RequestBody CardUpdateRequest request) {
        return cardService.update(id, request);
    }

    @PostMapping("/{id}/request-block")
    @Operation(
        summary = "Запрос блокировки карты", 
        description = "Пользовательский запрос на блокировку карты",
        responses = {
            @ApiResponse(responseCode = "200", description = "Запрос на блокировку отправлен", content = @Content(schema = @Schema(implementation = CardBlockResponse.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидная причина блокировки", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав на запрос блокировки", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public ResponseEntity<CardBlockResponse> requestCardBlock(
            @PathVariable UUID id,
            @RequestBody CardBlockRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        var response = cardService.requestCardBlock(id, userDetails.getUsername(), requestDto.reason());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Удаление банковской карты", 
        description = "Удалить карту",
        responses = {
            @ApiResponse(responseCode = "204", description = "Карта успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав на удаление", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public void delete(@PathVariable UUID id) {
        cardService.delete(id);
    }

    @PostMapping("/transfer")
    @Operation(
        summary = "Перевод с одной карты на другую", 
        description = "Перевод денег с одной банковской карты пользователя на другую",
        responses = {
            @ApiResponse(responseCode = "200", description = "Перевод выполнен успешно", content = @Content(schema = @Schema(implementation = TransferResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидные данные перевода", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно средств или нет прав", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Исходная или целевая карта не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        TransferResponse result = cardService.transfer(request, userDetails.getUsername());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/balance")
    @Operation(
        summary = "Получение баланса", 
        description = "Получить баланс с карты",
        responses = {
            @ApiResponse(responseCode = "200", description = "Баланс успешно получен", content = @Content(schema = @Schema(type = "number", format = "decimal"))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public ResponseEntity<BigDecimal> getBalance(@PathVariable UUID id) {
        var balance = cardService.getBalance(id);
        return ResponseEntity.ok(balance);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Изменение статуса карты", 
        description = "Изменить статус карты",
        responses = {
            @ApiResponse(responseCode = "200", description = "Статус карты изменён", content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидный статус", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав на изменение статуса", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public CardResponse updateStatus(
            @PathVariable UUID id,
            @RequestBody CardStatus status
    ) {
        return cardService.updateStatus(id, status);
    }

    @PutMapping("/admin/block-requests/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Одобрение блокировки карты", 
        description = "Одобрить и заблокировать карту",
        responses = {
            @ApiResponse(responseCode = "200", description = "Блокировка одобрена, карта заблокирована", content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "Запрос на блокировку не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Карта уже заблокирована", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав на одобрение", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public ResponseEntity<String> approveBlockRequest(@PathVariable UUID id) {
        adminCardService.approveBlockRequest(id);
        return ResponseEntity.ok("Блокировка одобрена, карта заблокирована.");
    }

    @PutMapping("/admin/block-requests/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Отклонение запроса на блокировку", 
        description = "Отклонить запрос блокировки карты",
        responses = {
            @ApiResponse(responseCode = "200", description = "Запрос на блокировку отклонён", content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "Запрос на блокировку не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Причина отклонения не указана", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав на отклонение", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public ResponseEntity<String> rejectBlockRequest(@PathVariable UUID id, @RequestBody String reason) {
        adminCardService.rejectBlockRequest(id, reason);
        return ResponseEntity.ok("Запрос на блокировку отклонён.");
    }
}
