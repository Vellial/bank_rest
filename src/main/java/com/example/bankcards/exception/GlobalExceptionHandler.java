package com.example.bankcards.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.bankcards.dto.ErrorResponse;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    // Валидация @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "Ошибка валидации"
                ));

        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Ошибка валидации", errors));
    }

    // Карта не найдена
    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCardNotFound(CardNotFoundException ex) {
        Map<String, String> errors = Map.of("cardNumber", ex.getNumber());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), errors));
    }
    @ExceptionHandler(CardByIdNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCardNotFound(CardByIdNotFoundException ex) {
        Map<String, String> errors = Map.of("cardId", ex.getId().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), errors));
    }

    // Недостаточно средств
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        Map<String, String> errors = Map.of("balance", "Недостаточно средств на карте",
                        "requestedAmount", String.valueOf(ex.getRequested()),
                        "Available", String.valueOf(ex.getAvailable()));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ex.getMessage(), errors));
    }

    // Доступ запрещён
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Доступ запрещён", null));
    }

    // Общая ошибка
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Внутренняя ошибка сервера", null));
    }
}
