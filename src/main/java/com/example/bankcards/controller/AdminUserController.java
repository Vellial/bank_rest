package com.example.bankcards.controller;

import com.example.bankcards.dto.ErrorResponse;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.user.UserUpdateRequest;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.management.relation.RoleNotFoundException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Управление пользователями")
public class AdminUserController {

    private final UserService userService;
    private final CardService cardService;

    @GetMapping
    @Operation(
        summary = "Все зарегистрированные пользователи",
        description = "Возвращает всех зарегистрированных пользователей",
        responses = {
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён (требуется роль ADMIN)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public Page<UserResponse> getAll(@PageableDefault Pageable pageable) {
        return userService.getAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Поиск пользователя по Id",
        description = "Возвращает пользователя, если он найден",
        responses = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public UserResponse getById(@PathVariable UUID id) {
        return userService.getById(id);
    }

    @GetMapping("/cards")
    @Operation(
        summary = "Все карты",
        description = "Возвращает все карты",
        responses = {
            @ApiResponse(responseCode = "200", description = "Список карт успешно получен", content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён (требуется роль ADMIN)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public Page<CardResponse> getAllCards(@PageableDefault Pageable pageable) {
        return cardService.getAllCards(pageable);
    }

    @PutMapping("/{id}/block")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Блокировка пользователя",
        description = "Блокирует пользователя",
        responses = {
            @ApiResponse(responseCode = "200", description = "Пользователь заблокирован"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав на блокировку", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public void blockUser(@PathVariable UUID id) {
        userService.blockUser(id);
    }

    @PutMapping("/{id}/unblock")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Снятие блокировки пользователя",
        description = "Разблокирует пользователя",
        responses = {
            @ApiResponse(responseCode = "200", description = "Пользователь разблокирован"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав на разблокировку", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public void unblockUser(@PathVariable UUID id) {
        userService.unblockUser(id);
    }

    @PutMapping("/{id}/role")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Обновление данных пользователя",
        description = "Обновить данные пользователя",
        responses = {
            @ApiResponse(responseCode = "200", description = "Пользователь разблокирован"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав на разблокировку", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public void updateUser(@PathVariable UUID id, @RequestBody UserUpdateRequest request) throws RoleNotFoundException {
        userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Удаление пользователя из системы",
        description = "Удаляет пользователя",
        responses = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удалён"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав на удаление", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public void deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
    }

}